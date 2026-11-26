package com.folio.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "library_settings")
public class LibrarySettings {

    static final short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column(name = "loan_days", nullable = false)
    private short loanDays;

    @Column(name = "renewal_days", nullable = false)
    private short renewalDays;

    @Column(name = "max_active_loans", nullable = false)
    private short maxActiveLoans;

    @Column(name = "fine_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal finePerDay;

    @Column(name = "fine_block_threshold", nullable = false, precision = 10, scale = 2)
    private BigDecimal fineBlockThreshold;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    protected LibrarySettings() {
    }

    void update(int loanDays, int renewalDays, int maxActiveLoans, BigDecimal finePerDay, BigDecimal fineBlockThreshold,
                Instant now, long actorId) {
        this.loanDays = (short) loanDays;
        this.renewalDays = (short) renewalDays;
        this.maxActiveLoans = (short) maxActiveLoans;
        this.finePerDay = finePerDay;
        this.fineBlockThreshold = fineBlockThreshold;
        this.updatedAt = now;
        this.updatedBy = actorId;
    }

    public int getLoanDays() {
        return loanDays;
    }

    public int getRenewalDays() {
        return renewalDays;
    }

    public int getMaxActiveLoans() {
        return maxActiveLoans;
    }

    public BigDecimal getFinePerDay() {
        return finePerDay;
    }

    public BigDecimal getFineBlockThreshold() {
        return fineBlockThreshold;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
