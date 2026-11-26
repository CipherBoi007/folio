package com.folio.circulation;

import com.folio.activity.ActivityAction;
import com.folio.activity.ActivityService;
import com.folio.auth.AuthenticatedUser;
import com.folio.auth.StaffUser;
import com.folio.auth.StaffUserRepository;
import com.folio.catalog.Book;
import com.folio.catalog.BookRepository;
import com.folio.circulation.CirculationDtos.BookRef;
import com.folio.circulation.CirculationDtos.Eligibility;
import com.folio.circulation.CirculationDtos.FineRef;
import com.folio.circulation.CirculationDtos.IssueRequest;
import com.folio.circulation.CirculationDtos.LoanFilter;
import com.folio.circulation.CirculationDtos.LoanStatus;
import com.folio.circulation.CirculationDtos.LoanView;
import com.folio.circulation.CirculationDtos.MemberRef;
import com.folio.circulation.CirculationDtos.ReturnResult;
import com.folio.circulation.CirculationDtos.StaffRef;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.common.web.PageResponse;
import com.folio.inventory.InventoryLedger;
import com.folio.inventory.StockChange;
import com.folio.inventory.TransactionType;
import com.folio.member.Member;
import com.folio.member.MemberRepository;
import com.folio.member.MemberStatus;
import com.folio.settings.SettingsService;
import com.folio.settings.SettingsService.Rules;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issue, return and renew (rules R4–R6). Rows are locked in a fixed order (member, then book; or loan, then book)
 * so concurrent desk operations serialise instead of overselling the last copy.
 */
@Service
public class CirculationService {

    private static final Logger log = LoggerFactory.getLogger(CirculationService.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private final LoanRepository loans;
    private final FineRepository fines;
    private final MemberRepository members;
    private final BookRepository books;
    private final StaffUserRepository staff;
    private final InventoryLedger ledger;
    private final SettingsService settings;
    private final ActivityService activity;
    private final WaitlistService waitlist;
    private final Clock clock;

    public CirculationService(LoanRepository loans, FineRepository fines, MemberRepository members, BookRepository books,
                              StaffUserRepository staff, InventoryLedger ledger, SettingsService settings,
                              ActivityService activity, WaitlistService waitlist, Clock clock) {
        this.loans = loans;
        this.fines = fines;
        this.members = members;
        this.books = books;
        this.staff = staff;
        this.ledger = ledger;
        this.settings = settings;
        this.activity = activity;
        this.waitlist = waitlist;
        this.clock = clock;
    }

    @Transactional
    public LoanView issue(IssueRequest request, AuthenticatedUser actor) {
        Rules rules = settings.current();
        LocalDate today = LocalDate.now(clock);

        Member member = members.findLiveForUpdate(request.memberId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.MEMBER_NOT_FOUND, request.memberId()));
        requireActive(member);
        Book book = books.findLiveForUpdate(request.bookId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.BOOK_NOT_FOUND, request.bookId()));

        Eligibility eligibility = evaluate(member, rules, today);
        if (!eligibility.canBorrow()) {
            Eligibility.Reason reason = eligibility.reasons().getFirst();
            throw new ApiException(ErrorCode.valueOf(reason.code()), reason.message(),
                    Map.of("activeLoans", eligibility.activeLoans(), "maxLoans", eligibility.maxLoans()));
        }
        if (loans.existsActive(member.getId(), book.getId())) {
            throw new ApiException(ErrorCode.ALREADY_BORROWED,
                    "%s already has \"%s\" on loan.".formatted(member.getFullName(), book.getTitle()));
        }

        StockChange change = book.checkOut();
        Loan loan = loans.saveAndFlush(new Loan(book, member, Instant.now(clock), today.plusDays(rules.loanDays()), actor.id()));
        ledger.record(book.getId(), TransactionType.ISSUE, change, null, null, loan.getId(), actor);
        waitlist.fulfil(book.getId(), member.getId());
        activity.record(actor, ActivityAction.LOAN_ISSUED, "LOAN", loan.getId(),
                "Issued \"%s\" to %s, due %s".formatted(book.getTitle(), member.getFullName(), DATE.format(loan.getDueDate())),
                Map.of("bookId", book.getId(), "memberId", member.getId(), "dueDate", loan.getDueDate().toString()));
        log.info("Loan {} issued: book {} to member {}", loan.getId(), book.getId(), member.getId());
        return views(List.of(loan), rules, today).getFirst();
    }

    @Transactional
    public ReturnResult returnLoan(long loanId, AuthenticatedUser actor) {
        Rules rules = settings.current();
        LocalDate today = LocalDate.now(clock);
        Instant now = Instant.now(clock);

        Loan loan = loans.findForUpdate(loanId).orElseThrow(() -> ApiException.notFound(ErrorCode.LOAN_NOT_FOUND, loanId));
        if (!loan.isActive()) {
            throw new ApiException(ErrorCode.ALREADY_RETURNED,
                    "This loan was already returned on %s.".formatted(DATE.format(loan.getReturnedAt().atZone(clock.getZone()))));
        }
        Book book = books.findByIdForUpdate(loan.getBook().getId()).orElseThrow();
        StockChange change = book.checkIn();
        loan.markReturned(now, actor.id());
        ledger.record(book.getId(), TransactionType.RETURN, change, null, null, loan.getId(), actor);

        Member member = loan.getMember();
        Fine fine = null;
        int daysLate = loan.daysLateOn(today);
        if (daysLate > 0) {
            fine = fines.saveAndFlush(new Fine(loan, daysLate, rules.finePerDay(), now));
            activity.record(actor, ActivityAction.FINE_CREATED, "FINE", fine.getId(),
                    "Fined %s ₹%s for returning \"%s\" %d %s late".formatted(member.getFullName(),
                            fine.getAmount().toPlainString(), book.getTitle(), daysLate, daysLate == 1 ? "day" : "days"),
                    Map.of("loanId", loan.getId(), "overdueDays", daysLate, "amount", fine.getAmount()));
        }
        activity.record(actor, ActivityAction.LOAN_RETURNED, "LOAN", loan.getId(),
                "Returned \"%s\" from %s%s".formatted(book.getTitle(), member.getFullName(),
                        daysLate > 0 ? ", %d %s late".formatted(daysLate, daysLate == 1 ? "day" : "days") : ""),
                Map.of("bookId", book.getId(), "memberId", member.getId(), "daysLate", daysLate));

        LoanView view = views(List.of(loan), rules, today).getFirst();
        return new ReturnResult(view, fine == null ? null : FineService.view(fine, Map.of()), waitlist.nextInQueue(book.getId()).orElse(null));
    }

    @Transactional
    public LoanView renew(long loanId, AuthenticatedUser actor) {
        Rules rules = settings.current();
        LocalDate today = LocalDate.now(clock);
        Loan loan = loans.findForUpdate(loanId).orElseThrow(() -> ApiException.notFound(ErrorCode.LOAN_NOT_FOUND, loanId));
        if (!loan.isActive()) {
            throw new ApiException(ErrorCode.ALREADY_RETURNED, "This loan has already been returned.");
        }
        requireActive(loan.getMember());
        if (loan.isOverdueOn(today)) {
            throw new ApiException(ErrorCode.LOAN_OVERDUE,
                    "This loan was due on %s. Overdue loans can't be renewed; record the return instead."
                            .formatted(DATE.format(loan.getDueDate())));
        }
        if (loan.getRenewalCount() >= 1) {
            throw new ApiException(ErrorCode.RENEWAL_LIMIT_REACHED, "This loan has already been renewed once.");
        }
        if (waitlist.anyWaiting(loan.getBook().getId())) {
            throw new ApiException(ErrorCode.BOOK_RESERVED, "Another member is waiting for \"%s\", so it can't be renewed. Ask for it back by the due date."
                    .formatted(loan.getBook().getTitle()));
        }
        LocalDate previousDue = loan.getDueDate();
        loan.renew(rules.renewalDays());
        activity.record(actor, ActivityAction.LOAN_RENEWED, "LOAN", loan.getId(),
                "Renewed \"%s\" for %s until %s".formatted(loan.getBook().getTitle(), loan.getMember().getFullName(),
                        DATE.format(loan.getDueDate())),
                Map.of("previousDueDate", previousDue.toString(), "dueDate", loan.getDueDate().toString()));
        return views(List.of(loan), rules, today).getFirst();
    }

    @Transactional(readOnly = true)
    public Eligibility eligibility(long memberId) {
        Member member = members.findLive(memberId).orElseThrow(() -> ApiException.notFound(ErrorCode.MEMBER_NOT_FOUND, memberId));
        return evaluate(member, settings.current(), LocalDate.now(clock));
    }

    @Transactional(readOnly = true)
    public LoanView get(long id) {
        Loan loan = loans.findWithDetails(id).orElseThrow(() -> ApiException.notFound(ErrorCode.LOAN_NOT_FOUND, id));
        return views(List.of(loan), settings.current(), LocalDate.now(clock)).getFirst();
    }

    @Transactional(readOnly = true)
    public PageResponse<LoanView> search(LoanFilter filter, Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        Page<Loan> page = loans.findAll(matching(filter, today), pageable);
        List<LoanView> views = views(page.getContent(), settings.current(), today);
        return new PageResponse<>(views, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public void stream(LoanFilter filter, Sort sort, Consumer<LoanView> consumer) {
        LocalDate today = LocalDate.now(clock);
        views(loans.findAll(matching(filter, today), sort), settings.current(), today).forEach(consumer);
    }

    private Eligibility evaluate(Member member, Rules rules, LocalDate today) {
        long id = member.getId();
        int active = (int) loans.countActive(id);
        long overdue = loans.countOverdue(id, today);
        BigDecimal unpaid = fines.unpaidTotal(id);
        String name = member.getFullName();

        List<Eligibility.Reason> reasons = new ArrayList<>();
        if (!member.isActive()) {
            reasons.add(reason(ErrorCode.MEMBER_INACTIVE, inactiveMessage(member)));
        }
        if (overdue > 0) {
            reasons.add(reason(ErrorCode.HAS_OVERDUE_LOANS, "%s has %d overdue %s. Record the %s before issuing another."
                    .formatted(name, overdue, overdue == 1 ? "book" : "books", overdue == 1 ? "return" : "returns")));
        }
        if (unpaid.compareTo(rules.fineBlockThreshold()) > 0) {
            reasons.add(reason(ErrorCode.FINE_LIMIT_EXCEEDED, "%s owes ₹%s in unpaid fines, over the ₹%s limit. Collect payment before issuing."
                    .formatted(name, unpaid.toPlainString(), rules.fineBlockThreshold().toPlainString())));
        }
        if (active >= rules.maxActiveLoans()) {
            reasons.add(reason(ErrorCode.BORROW_LIMIT_REACHED, "%s already has %d %s on loan. Return one before issuing another."
                    .formatted(name, active, active == 1 ? "book" : "books")));
        }
        return new Eligibility(reasons.isEmpty(), active, rules.maxActiveLoans(), Math.max(0, rules.maxActiveLoans() - active),
                overdue, unpaid, rules.fineBlockThreshold(), rules.loanDays(), today.plusDays(rules.loanDays()), reasons);
    }

    private void requireActive(Member member) {
        if (!member.isActive()) {
            throw new ApiException(ErrorCode.MEMBER_INACTIVE, inactiveMessage(member));
        }
    }

    private static String inactiveMessage(Member member) {
        String state = member.getStatus() == MemberStatus.SUSPENDED ? "suspended" : "inactive";
        return "%s's membership is %s. Reactivate it before issuing books.".formatted(member.getFullName(), state);
    }

    private static Eligibility.Reason reason(ErrorCode code, String message) {
        return new Eligibility.Reason(code.name(), message);
    }

    List<LoanView> views(List<Loan> page, Rules rules, LocalDate today) {
        if (page.isEmpty()) {
            return List.of();
        }
        Map<Long, Fine> finesByLoan = fines.findByLoanIds(page.stream().map(Loan::getId).toList()).stream()
                .collect(Collectors.toMap(f -> f.getLoan().getId(), Function.identity()));
        Set<Long> staffIds = new HashSet<>();
        page.forEach(l -> {
            staffIds.add(l.getIssuedBy());
            if (l.getReturnedBy() != null) {
                staffIds.add(l.getReturnedBy());
            }
        });
        Map<Long, String> names = staffNames(staffIds);

        return page.stream().map(loan -> {
            boolean active = loan.isActive();
            int lateDays = active ? loan.daysLateOn(today) : 0;
            Fine fine = finesByLoan.get(loan.getId());
            int overdueDays = active ? lateDays : (fine == null ? 0 : fine.getOverdueDays());
            BigDecimal projected = active && lateDays > 0 ? rules.finePerDay().multiply(BigDecimal.valueOf(lateDays)) : null;
            return new LoanView(
                    loan.getId(),
                    BookRef.from(loan.getBook()),
                    MemberRef.from(loan.getMember()),
                    loan.getIssuedAt(),
                    loan.getDueDate(),
                    loan.getReturnedAt(),
                    loan.getRenewalCount(),
                    active ? LoanStatus.ACTIVE : LoanStatus.RETURNED,
                    loan.isOverdueOn(today),
                    active && loan.getDueDate().isEqual(today),
                    overdueDays,
                    projected,
                    active && !loan.isOverdueOn(today) && loan.getRenewalCount() < 1,
                    new StaffRef(loan.getIssuedBy(), names.get(loan.getIssuedBy())),
                    loan.getReturnedBy() == null ? null : new StaffRef(loan.getReturnedBy(), names.get(loan.getReturnedBy())),
                    fine == null ? null : new FineRef(fine.getId(), fine.getAmount(), fine.getStatus()));
        }).toList();
    }

    private Map<Long, String> staffNames(Collection<Long> ids) {
        return staff.findAllById(ids).stream().collect(Collectors.toMap(StaffUser::getId, StaffUser::getName));
    }

    private static Specification<Loan> matching(LoanFilter filter, LocalDate today) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.memberId() != null) {
                predicates.add(cb.equal(root.get("member").get("id"), filter.memberId()));
            }
            if (filter.bookId() != null) {
                predicates.add(cb.equal(root.get("book").get("id"), filter.bookId()));
            }
            if (filter.status() == LoanStatus.ACTIVE) {
                predicates.add(cb.isNull(root.get("returnedAt")));
            } else if (filter.status() == LoanStatus.RETURNED) {
                predicates.add(cb.isNotNull(root.get("returnedAt")));
            }
            if (Boolean.TRUE.equals(filter.overdue())) {
                predicates.add(cb.isNull(root.get("returnedAt")));
                predicates.add(cb.lessThan(root.get("dueDate"), today));
            }
            if (Boolean.TRUE.equals(filter.dueToday())) {
                predicates.add(cb.isNull(root.get("returnedAt")));
                predicates.add(cb.equal(root.get("dueDate"), today));
            }
            if (filter.q() != null && !filter.q().isBlank()) {
                String term = filter.q().trim().toLowerCase(Locale.ROOT);
                String like = "%" + term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                var book = root.join("book", JoinType.INNER);
                var member = root.join("member", JoinType.INNER);
                List<Predicate> any = new ArrayList<>();
                any.add(cb.like(cb.lower(book.get("title")), like, '\\'));
                any.add(cb.like(cb.lower(member.get("fullName")), like, '\\'));
                any.add(cb.like(cb.lower(member.get("membershipNumber")), like, '\\'));
                String digits = term.replaceAll("[\\s-]", "");
                if (digits.matches("\\d+")) {
                    any.add(cb.like(book.get("isbn13"), "%" + digits + "%"));
                    if (digits.length() <= 12) {
                        any.add(cb.equal(root.get("id"), Long.parseLong(digits)));
                    }
                }
                predicates.add(cb.or(any.toArray(Predicate[]::new)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
