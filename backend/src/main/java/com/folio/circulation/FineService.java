package com.folio.circulation;

import com.folio.activity.ActivityAction;
import com.folio.activity.ActivityService;
import com.folio.auth.AuthenticatedUser;
import com.folio.auth.StaffUser;
import com.folio.auth.StaffUserRepository;
import com.folio.circulation.CirculationDtos.BookRef;
import com.folio.circulation.CirculationDtos.FineFilter;
import com.folio.circulation.CirculationDtos.FineSummary;
import com.folio.circulation.CirculationDtos.FineView;
import com.folio.circulation.CirculationDtos.MemberRef;
import com.folio.circulation.CirculationDtos.StaffRef;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.common.web.PageResponse;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FineService {

    private final FineRepository fines;
    private final StaffUserRepository staff;
    private final ActivityService activity;
    private final Clock clock;

    public FineService(FineRepository fines, StaffUserRepository staff, ActivityService activity, Clock clock) {
        this.fines = fines;
        this.staff = staff;
        this.activity = activity;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<FineView> search(FineFilter filter, Pageable pageable) {
        Page<Fine> page = fines.findAll(matching(filter), pageable);
        List<Long> resolverIds = page.getContent().stream().map(Fine::getResolvedBy).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> names = staff.findAllById(resolverIds).stream().collect(Collectors.toMap(StaffUser::getId, StaffUser::getName));
        return PageResponse.of(page, f -> view(f, names));
    }

    @Transactional(readOnly = true)
    public FineSummary summary() {
        Map<FineStatus, FineRepository.StatusTotal> totals = fines.totalsByStatus().stream()
                .collect(Collectors.toMap(FineRepository.StatusTotal::getStatus, t -> t));
        return new FineSummary(totals(totals.get(FineStatus.UNPAID)), totals(totals.get(FineStatus.PAID)), totals(totals.get(FineStatus.WAIVED)));
    }

    @Transactional
    public FineView pay(long id, String note, AuthenticatedUser actor) {
        Fine fine = lockUnpaid(id);
        fine.pay(Instant.now(clock), actor.id(), blankToNull(note));
        activity.record(actor, ActivityAction.FINE_PAID, "FINE", id,
                "Collected ₹%s from %s".formatted(fine.getAmount().toPlainString(), fine.getLoan().getMember().getFullName()),
                Map.of("amount", fine.getAmount()));
        return view(fine, Map.of(actor.id(), actor.name()));
    }

    @Transactional
    public FineView waive(long id, String reason, AuthenticatedUser actor) {
        Fine fine = lockUnpaid(id);
        fine.waive(Instant.now(clock), actor.id(), reason.trim());
        activity.record(actor, ActivityAction.FINE_WAIVED, "FINE", id,
                "Waived ₹%s for %s: %s".formatted(fine.getAmount().toPlainString(), fine.getLoan().getMember().getFullName(), reason.trim()),
                Map.of("amount", fine.getAmount(), "reason", reason.trim()));
        return view(fine, Map.of(actor.id(), actor.name()));
    }

    private Fine lockUnpaid(long id) {
        Fine fine = fines.findForUpdate(id).orElseThrow(() -> ApiException.notFound(ErrorCode.FINE_NOT_FOUND, id));
        if (fine.getStatus() != FineStatus.UNPAID) {
            throw new ApiException(ErrorCode.FINE_ALREADY_RESOLVED,
                    "This fine was already %s.".formatted(fine.getStatus() == FineStatus.PAID ? "paid" : "waived"));
        }
        return fine;
    }

    static FineView view(Fine f, Map<Long, String> staffNames) {
        Loan loan = f.getLoan();
        return new FineView(f.getId(), loan.getId(), MemberRef.from(loan.getMember()), BookRef.from(loan.getBook()),
                f.getOverdueDays(), f.getDailyRate(), f.getAmount(), f.getStatus(), f.getCreatedAt(), f.getResolvedAt(),
                f.getResolvedBy() == null ? null : new StaffRef(f.getResolvedBy(), staffNames.get(f.getResolvedBy())),
                f.getResolutionNote());
    }

    private static Specification<Fine> matching(FineFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.memberId() != null) {
                predicates.add(cb.equal(root.get("loan").get("member").get("id"), filter.memberId()));
            }
            if (filter.q() != null && !filter.q().isBlank()) {
                String like = "%" + filter.q().trim().toLowerCase(Locale.ROOT).replace("%", "\\%").replace("_", "\\_") + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("loan").get("member").get("fullName")), like, '\\'),
                        cb.like(cb.lower(root.get("loan").get("member").get("membershipNumber")), like, '\\'),
                        cb.like(cb.lower(root.get("loan").get("book").get("title")), like, '\\')));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static FineSummary.Totals totals(FineRepository.StatusTotal total) {
        return total == null ? new FineSummary.Totals(0, BigDecimal.ZERO.setScale(2)) : new FineSummary.Totals(total.getCount(), total.getTotal());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
