package com.folio.circulation;

import com.folio.catalog.Author;
import com.folio.catalog.Book;
import com.folio.member.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class CirculationDtos {

    private CirculationDtos() {
    }

    public enum LoanStatus {
        ACTIVE, RETURNED
    }

    public record BookRef(long id, String title, String isbn13, List<String> authors, String coverUrl) {

        static BookRef from(Book b) {
            return new BookRef(b.getId(), b.getTitle(), b.getIsbn13(), b.getAuthors().stream().map(Author::getName).toList(), b.getCoverUrl());
        }
    }

    public record MemberRef(long id, String fullName, String membershipNumber) {

        static MemberRef from(Member m) {
            return new MemberRef(m.getId(), m.getFullName(), m.getMembershipNumber());
        }
    }

    public record StaffRef(long id, String name) {
    }

    public record FineRef(long id, BigDecimal amount, FineStatus status) {
    }

    public record LoanView(
            long id,
            BookRef book,
            MemberRef member,
            Instant issuedAt,
            LocalDate dueDate,
            Instant returnedAt,
            int renewalCount,
            LoanStatus status,
            boolean overdue,
            boolean dueToday,
            int overdueDays,
            BigDecimal projectedFine,
            boolean renewable,
            StaffRef issuedBy,
            StaffRef returnedBy,
            FineRef fine) {
    }

    public record FineView(
            long id,
            long loanId,
            MemberRef member,
            BookRef book,
            int overdueDays,
            BigDecimal dailyRate,
            BigDecimal amount,
            FineStatus status,
            Instant createdAt,
            Instant resolvedAt,
            StaffRef resolvedBy,
            String resolutionNote) {
    }

    public record FineSummary(Totals unpaid, Totals paid, Totals waived) {

        public record Totals(long count, BigDecimal amount) {
        }
    }

    public record IssueRequest(@NotNull(message = "Choose a member.") Long memberId, @NotNull(message = "Choose a book.") Long bookId) {
    }

    public record WaiveRequest(@NotBlank(message = "Give a reason for waiving this fine.") @Size(max = 500) String reason) {
    }

    public record PayRequest(@Size(max = 500) String note) {
    }

    public record ReturnResult(LoanView loan, FineView fine, MemberRef nextInQueue) {
    }

    public record Eligibility(
            boolean canBorrow,
            int activeLoans,
            int maxLoans,
            int remainingSlots,
            long overdueLoans,
            BigDecimal unpaidFines,
            BigDecimal fineLimit,
            int loanDays,
            LocalDate dueDateIfIssuedToday,
            List<Reason> reasons) {

        public record Reason(String code, String message) {
        }
    }

    record LoanFilter(Long memberId, Long bookId, LoanStatus status, Boolean overdue, Boolean dueToday, String q) {
    }

    record FineFilter(FineStatus status, Long memberId, String q) {
    }
}
