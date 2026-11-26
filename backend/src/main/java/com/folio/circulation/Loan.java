package com.folio.circulation;

import com.folio.catalog.Book;
import com.folio.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, updatable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private Instant returnedAt;

    @Column(name = "renewal_count", nullable = false)
    private short renewalCount;

    @Column(name = "issued_by", nullable = false, updatable = false)
    private long issuedBy;

    @Column(name = "returned_by")
    private Long returnedBy;

    protected Loan() {
    }

    Loan(Book book, Member member, Instant issuedAt, LocalDate dueDate, long issuedBy) {
        this.book = book;
        this.member = member;
        this.issuedAt = issuedAt;
        this.dueDate = dueDate;
        this.issuedBy = issuedBy;
    }

    public boolean isActive() {
        return returnedAt == null;
    }

    public boolean isOverdueOn(LocalDate today) {
        return isActive() && dueDate.isBefore(today);
    }

    /** Whole days late if returned on {@code returnDate}; zero when on time. */
    public int daysLateOn(LocalDate returnDate) {
        long days = ChronoUnit.DAYS.between(dueDate, returnDate);
        return days > 0 ? (int) days : 0;
    }

    void markReturned(Instant now, long actorId) {
        this.returnedAt = now;
        this.returnedBy = actorId;
    }

    void renew(int days) {
        this.dueDate = dueDate.plusDays(days);
        this.renewalCount++;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public long getIssuedBy() {
        return issuedBy;
    }

    public Long getReturnedBy() {
        return returnedBy;
    }
}
