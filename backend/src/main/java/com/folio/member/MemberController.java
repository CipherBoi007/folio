package com.folio.member;

import com.folio.auth.AuthenticatedUser;
import com.folio.common.csv.CsvWriter;
import com.folio.common.web.PageResponse;
import com.folio.common.web.Pagination;
import com.folio.member.MemberDtos.MemberDetail;
import com.folio.member.MemberDtos.MemberRequest;
import com.folio.member.MemberDtos.MemberSummary;
import com.folio.member.MemberDtos.StatusRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "Members")
public class MemberController {

    private static final Map<String, String> SORTS = Map.of(
            "name", "fullName",
            "number", "membershipNumber",
            "joined", "joinedOn",
            "added", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by("fullName").and(Sort.by("id"));

    private final MemberService members;

    public MemberController(MemberService members) {
        this.members = members;
    }

    @GetMapping
    public PageResponse<MemberSummary> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return members.search(q, status, Pagination.pageable(page, size, sort, SORTS, DEFAULT_SORT));
    }

    @GetMapping("/export.csv")
    public void export(@RequestParam(required = false) String q, @RequestParam(required = false) MemberStatus status,
                       HttpServletResponse response) throws IOException {
        CsvWriter csv = CsvWriter.attachment(response, "folio-members.csv")
                .row("Membership no.", "Name", "Email", "Phone", "City", "Status", "Joined", "Active loans", "Overdue", "Unpaid fines");
        members.stream(q, status, DEFAULT_SORT, m -> csv.row(m.membershipNumber(), m.fullName(), m.email(), m.phone(), m.city(),
                m.status(), m.joinedOn(), m.activeLoans(), m.overdueLoans(), m.unpaidFines()));
        csv.flush();
    }

    @GetMapping("/{id}")
    public MemberDetail get(@PathVariable long id) {
        return members.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDetail create(@Valid @RequestBody MemberRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return members.create(request, actor);
    }

    @PutMapping("/{id}")
    public MemberDetail update(@PathVariable long id, @Valid @RequestBody MemberRequest request,
                               @AuthenticationPrincipal AuthenticatedUser actor) {
        return members.update(id, request, actor);
    }

    @PatchMapping("/{id}/status")
    public MemberDetail changeStatus(@PathVariable long id, @Valid @RequestBody StatusRequest request,
                                     @AuthenticationPrincipal AuthenticatedUser actor) {
        return members.changeStatus(id, request.status(), actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        members.delete(id, actor);
    }
}
