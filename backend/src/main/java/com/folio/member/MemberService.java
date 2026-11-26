package com.folio.member;

import com.folio.activity.ActivityAction;
import com.folio.activity.ActivityService;
import com.folio.auth.AuthenticatedUser;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.common.web.PageResponse;
import com.folio.member.MemberDtos.MemberDetail;
import com.folio.member.MemberDtos.MemberRequest;
import com.folio.member.MemberDtos.MemberSummary;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository members;
    private final ActivityService activity;
    private final Clock clock;

    public MemberService(MemberRepository members, ActivityService activity, Clock clock) {
        this.members = members;
        this.activity = activity;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberSummary> search(String q, MemberStatus status, Pageable pageable) {
        Page<Member> page = members.findAll(matching(q, status), pageable);
        Stats stats = stats(page.getContent().stream().map(Member::getId).toList());
        return PageResponse.of(page, m -> summary(m, stats));
    }

    @Transactional(readOnly = true)
    public void stream(String q, MemberStatus status, Sort sort, Consumer<MemberSummary> consumer) {
        List<Member> all = members.findAll(matching(q, status), sort);
        Stats stats = stats(all.stream().map(Member::getId).toList());
        all.forEach(m -> consumer.accept(summary(m, stats)));
    }

    @Transactional(readOnly = true)
    public MemberDetail get(long id) {
        Member m = members.findLive(id).orElseThrow(() -> ApiException.notFound(ErrorCode.MEMBER_NOT_FOUND, id));
        Stats stats = stats(List.of(id));
        return new MemberDetail(m.getId(), m.getMembershipNumber(), m.getFullName(), m.getEmail(), m.getPhone(),
                m.getAddressLine(), m.getCity(), m.getPostalCode(), m.getStatus(), m.getJoinedOn(),
                stats.active(id), stats.overdue(id), members.countAllLoans(id), stats.unpaid(id), m.getCreatedAt(), m.getUpdatedAt());
    }

    @Transactional
    public MemberDetail create(MemberRequest request, AuthenticatedUser actor) {
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, null);
        LocalDate today = LocalDate.now(clock);
        String number = "FOL-%d-%04d".formatted(today.getYear(), members.nextMembershipSequence());
        Member member = new Member(number, today);
        describe(member, request, email);
        members.saveAndFlush(member);
        activity.record(actor, ActivityAction.MEMBER_CREATED, "MEMBER", member.getId(),
                "Registered %s (%s)".formatted(member.getFullName(), number), Map.of("membershipNumber", number));
        return get(member.getId());
    }

    @Transactional
    public MemberDetail update(long id, MemberRequest request, AuthenticatedUser actor) {
        Member member = members.findLiveForUpdate(id).orElseThrow(() -> ApiException.notFound(ErrorCode.MEMBER_NOT_FOUND, id));
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email, id);
        describe(member, request, email);
        members.flush();
        activity.record(actor, ActivityAction.MEMBER_UPDATED, "MEMBER", id, "Updated %s".formatted(member.getFullName()), Map.of());
        return get(id);
    }

    @Transactional
    public MemberDetail changeStatus(long id, MemberStatus status, AuthenticatedUser actor) {
        Member member = members.findLiveForUpdate(id).orElseThrow(() -> ApiException.notFound(ErrorCode.MEMBER_NOT_FOUND, id));
        if (member.getStatus() == status) {
            return get(id);
        }
        if (status == MemberStatus.INACTIVE) {
            requireNoActiveLoans(member, "deactivating");
        }
        MemberStatus previous = member.getStatus();
        member.changeStatus(status);
        activity.record(actor, ActivityAction.MEMBER_STATUS_CHANGED, "MEMBER", id,
                "Changed %s from %s to %s".formatted(member.getFullName(), label(previous), label(status)),
                Map.of("from", previous.name(), "to", status.name()));
        return get(id);
    }

    @Transactional
    public void delete(long id, AuthenticatedUser actor) {
        Member member = members.findLiveForUpdate(id).orElseThrow(() -> ApiException.notFound(ErrorCode.MEMBER_NOT_FOUND, id));
        requireNoActiveLoans(member, "deleting");
        BigDecimal unpaid = stats(List.of(id)).unpaid(id);
        if (unpaid.signum() > 0) {
            throw new ApiException(ErrorCode.MEMBER_HAS_UNPAID_FINES,
                    "%s has ₹%s in unpaid fines. Settle or waive them before deleting.".formatted(member.getFullName(), unpaid.toPlainString()));
        }
        member.markDeleted(Instant.now(clock));
        activity.record(actor, ActivityAction.MEMBER_DELETED, "MEMBER", id,
                "Removed %s (%s)".formatted(member.getFullName(), member.getMembershipNumber()), Map.of());
    }

    private void requireNoActiveLoans(Member member, String action) {
        long active = stats(List.of(member.getId())).active(member.getId());
        if (active > 0) {
            throw new ApiException(ErrorCode.MEMBER_HAS_ACTIVE_LOANS,
                    "%s has %d %s on loan. Record the %s before %s the membership."
                            .formatted(member.getFullName(), active, active == 1 ? "book" : "books", active == 1 ? "return" : "returns", action),
                    Map.of("activeLoans", active));
        }
    }

    private void describe(Member member, MemberRequest r, String email) {
        member.describe(r.fullName().trim().replaceAll("\\s+", " "), email, normalizePhone(r.phone()),
                blankToNull(r.addressLine()), blankToNull(r.city()), blankToNull(r.postalCode()));
    }

    private void ensureEmailAvailable(String email, Long selfId) {
        members.findByEmail(email).filter(existing -> !existing.getId().equals(selfId)).ifPresent(existing -> {
            throw ApiException.field(ErrorCode.DUPLICATE_EMAIL, "email",
                    "%s is already registered to %s (%s).".formatted(email, existing.getFullName(), existing.getMembershipNumber()));
        });
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** Accepts "98765 43210", "+91 98765-43210", "098765 43210"; stores the 10-digit mobile number. */
    static String normalizePhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        } else if (digits.length() == 11 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        if (!digits.matches("[6-9]\\d{9}")) {
            throw ApiException.field(ErrorCode.VALIDATION_FAILED, "phone", "Enter a 10-digit Indian mobile number starting with 6, 7, 8 or 9.");
        }
        return digits;
    }

    private Specification<Member> matching(String q, MemberStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (q != null && !q.isBlank()) {
                String term = q.trim().toLowerCase(Locale.ROOT);
                String like = "%" + term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                List<Predicate> any = new ArrayList<>();
                any.add(cb.like(cb.lower(root.get("fullName")), like, '\\'));
                any.add(cb.like(root.get("email"), like, '\\'));
                any.add(cb.like(cb.lower(root.get("membershipNumber")), like, '\\'));
                String digits = term.replaceAll("\\D", "");
                if (digits.length() >= 3) {
                    any.add(cb.like(root.get("phone"), "%" + digits + "%"));
                }
                predicates.add(cb.or(any.toArray(Predicate[]::new)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Stats stats(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return new Stats(Map.of(), Map.of());
        }
        Map<Long, MemberRepository.LoanCounts> loans = members.loanCounts(ids, LocalDate.now(clock)).stream()
                .collect(Collectors.toMap(MemberRepository.LoanCounts::getMemberId, Function.identity()));
        Map<Long, BigDecimal> fines = members.unpaidFines(ids).stream()
                .collect(Collectors.toMap(MemberRepository.FineTotal::getMemberId, MemberRepository.FineTotal::getUnpaid));
        return new Stats(loans, fines);
    }

    private static MemberSummary summary(Member m, Stats stats) {
        return new MemberSummary(m.getId(), m.getMembershipNumber(), m.getFullName(), m.getEmail(), m.getPhone(), m.getCity(),
                m.getStatus(), m.getJoinedOn(), stats.active(m.getId()), stats.overdue(m.getId()), stats.unpaid(m.getId()));
    }

    private static String label(MemberStatus status) {
        return status.name().charAt(0) + status.name().substring(1).toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record Stats(Map<Long, MemberRepository.LoanCounts> loans, Map<Long, BigDecimal> fines) {

        long active(long id) {
            var counts = loans.get(id);
            return counts == null ? 0 : counts.getActiveLoans();
        }

        long overdue(long id) {
            var counts = loans.get(id);
            return counts == null ? 0 : counts.getOverdueLoans();
        }

        BigDecimal unpaid(long id) {
            return fines.getOrDefault(id, BigDecimal.ZERO.setScale(2));
        }
    }
}
