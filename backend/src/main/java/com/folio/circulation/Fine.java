package com.folio.circulation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.Generated;

@Entity
@Table(name = "fines")
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false, updatable = false)
    private Loan loan;

    @Column(name = "overdue_days", nullable = false, updatable = false)
    private int overdueDays;

    @Column(name = "daily_rate", nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Generated
    @Column(insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FineStatus status = FineStatus.UNPAID;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolution_note", length = 500)
    private String resolutionNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Fine() {
    }

    Fine(Loan loan, int overdueDays, BigDecimal dailyRate, Instant createdAt) {
        this.loan = loan;
        this.overdueDays = overdueDays;
        this.dailyRate = dailyRate;
        this.createdAt = createdAt;
    }

    void pay(Instant now, long actorId, String note) {
        resolve(FineStatus.PAID, now, actorId, note);
    }

    void waive(Instant now, long actorId, String reason) {
        resolve(FineStatus.WAIVED, now, actorId, reason);
    }

    private void resolve(FineStatus target, Instant now, long actorId, String note) {
        this.status = target;
        this.resolvedAt = now;
        this.resolvedBy = actorId;
        this.resolutionNote = note;
    }

    public BigDecimal expectedAmount() {
        return dailyRate.multiply(BigDecimal.valueOf(overdueDays));
    }

    public Long getId() {
        return id;
    }

    public Loan getLoan() {
        return loan;
    }

    public int getOverdueDays() {
        return overdueDays;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public BigDecimal getAmount() {
        return amount != null ? amount : expectedAmount();
    }

    public FineStatus getStatus() {
        return status;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Long getResolvedBy() {
        return resolvedBy;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
