package com.folio.circulation;

import com.folio.auth.AuthenticatedUser;
import com.folio.circulation.CirculationDtos.Eligibility;
import com.folio.circulation.CirculationDtos.FineFilter;
import com.folio.circulation.CirculationDtos.FineSummary;
import com.folio.circulation.CirculationDtos.FineView;
import com.folio.circulation.CirculationDtos.IssueRequest;
import com.folio.circulation.CirculationDtos.LoanFilter;
import com.folio.circulation.CirculationDtos.LoanStatus;
import com.folio.circulation.CirculationDtos.LoanView;
import com.folio.circulation.CirculationDtos.PayRequest;
import com.folio.circulation.CirculationDtos.ReturnResult;
import com.folio.circulation.CirculationDtos.WaiveRequest;
import com.folio.common.csv.CsvWriter;
import com.folio.common.web.PageResponse;
import com.folio.common.web.Pagination;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Circulation")
public class CirculationController {

    private static final Map<String, String> LOAN_SORTS = Map.of("issued", "issuedAt", "due", "dueDate", "returned", "returnedAt");
    private static final Sort LOAN_DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "issuedAt").and(Sort.by(Sort.Direction.DESC, "id"));
    private static final Map<String, String> FINE_SORTS = Map.of("created", "createdAt", "amount", "amount", "resolved", "resolvedAt");
    private static final Sort FINE_DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));

    private final CirculationService circulation;
    private final FineService fines;
    private final WaitlistService waitlist;

    public CirculationController(CirculationService circulation, FineService fines, WaitlistService waitlist) {
        this.circulation = circulation;
        this.fines = fines;
        this.waitlist = waitlist;
    }

    @GetMapping("/books/{id}/waitlist")
    public List<WaitlistService.Entry> bookWaitlist(@PathVariable long id) {
        return waitlist.forBook(id);
    }

    @PostMapping("/books/{id}/waitlist")
    @ResponseStatus(HttpStatus.CREATED)
    public WaitlistService.Entry joinWaitlist(@PathVariable long id, @Valid @RequestBody WaitlistRequest request,
                                              @AuthenticationPrincipal AuthenticatedUser actor) {
        return waitlist.join(id, request.memberId(), actor);
    }

    @GetMapping("/members/{id}/waitlist")
    public List<WaitlistService.Entry> memberWaitlist(@PathVariable long id) {
        return waitlist.forMember(id);
    }

    @DeleteMapping("/waitlist/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelWaitlist(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        waitlist.cancel(id, actor);
    }

    public record WaitlistRequest(@NotNull(message = "Choose a member.") Long memberId) {
    }

    @PostMapping("/loans/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public LoanView issue(@Valid @RequestBody IssueRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return circulation.issue(request, actor);
    }

    @PostMapping("/loans/{id}/return")
    public ReturnResult returnLoan(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return circulation.returnLoan(id, actor);
    }

    @PostMapping("/loans/{id}/renew")
    public LoanView renew(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return circulation.renew(id, actor);
    }

    @GetMapping("/loans/{id}")
    public LoanView get(@PathVariable long id) {
        return circulation.get(id);
    }

    @GetMapping("/loans")
    public PageResponse<LoanView> list(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) Boolean dueToday,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return circulation.search(new LoanFilter(memberId, bookId, status, overdue, dueToday, q),
                Pagination.pageable(page, size, sort, LOAN_SORTS, LOAN_DEFAULT_SORT));
    }

    @GetMapping("/loans/export.csv")
    public void export(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) Boolean overdue,
            HttpServletResponse response) throws IOException {
        CsvWriter csv = CsvWriter.attachment(response, "folio-loans.csv")
                .row("Loan", "Book", "ISBN", "Member", "Membership no.", "Issued", "Due", "Returned", "Status", "Overdue days",
                        "Renewals", "Issued by");
        circulation.stream(new LoanFilter(memberId, bookId, status, overdue, null, null), LOAN_DEFAULT_SORT, l -> csv.row(
                l.id(), l.book().title(), l.book().isbn13(), l.member().fullName(), l.member().membershipNumber(), l.issuedAt(),
                l.dueDate(), l.returnedAt(), l.overdue() ? "OVERDUE" : l.status(), l.overdueDays(), l.renewalCount(), l.issuedBy().name()));
        csv.flush();
    }

    @GetMapping("/members/{id}/eligibility")
    public Eligibility eligibility(@PathVariable long id) {
        return circulation.eligibility(id);
    }

    @GetMapping("/members/{id}/loans")
    public PageResponse<LoanView> memberLoans(@PathVariable long id, @RequestParam(required = false) LoanStatus status,
                                              @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return circulation.search(new LoanFilter(id, null, status, null, null, null),
                Pagination.pageable(page, size, null, LOAN_SORTS, LOAN_DEFAULT_SORT));
    }

    @GetMapping("/books/{id}/loans")
    public PageResponse<LoanView> bookLoans(@PathVariable long id, @RequestParam(required = false) LoanStatus status,
                                            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return circulation.search(new LoanFilter(null, id, status, null, null, null),
                Pagination.pageable(page, size, null, LOAN_SORTS, LOAN_DEFAULT_SORT));
    }

    @GetMapping("/fines")
    public PageResponse<FineView> fines(
            @RequestParam(required = false) FineStatus status,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return fines.search(new FineFilter(status, memberId, q), Pagination.pageable(page, size, sort, FINE_SORTS, FINE_DEFAULT_SORT));
    }

    @GetMapping("/fines/summary")
    public FineSummary fineSummary() {
        return fines.summary();
    }

    @PostMapping("/fines/{id}/pay")
    public FineView pay(@PathVariable long id, @Valid @RequestBody(required = false) PayRequest request,
                        @AuthenticationPrincipal AuthenticatedUser actor) {
        return fines.pay(id, request == null ? null : request.note(), actor);
    }

    @PostMapping("/fines/{id}/waive")
    @PreAuthorize("hasRole('ADMIN')")
    public FineView waive(@PathVariable long id, @Valid @RequestBody WaiveRequest request,
                          @AuthenticationPrincipal AuthenticatedUser actor) {
        return fines.waive(id, request.reason(), actor);
    }
}
