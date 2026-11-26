package com.folio.circulation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoanTest {

    private static final LocalDate ISSUED = LocalDate.of(2026, 11, 1);

    private static Loan loan() {
        return new Loan(null, null, Instant.parse("2026-11-01T05:00:00Z"), ISSUED.plusDays(14), 1L);
    }

    @Test
    void overdueOnlyAfterTheDueDate() {
        Loan loan = loan();
        assertThat(loan.isOverdueOn(ISSUED.plusDays(14))).isFalse();
        assertThat(loan.isOverdueOn(ISSUED.plusDays(15))).isTrue();
    }

    @Test
    void daysLateCountsWholeDaysPastDue() {
        Loan loan = loan();
        assertThat(loan.daysLateOn(ISSUED.plusDays(10))).isZero();
        assertThat(loan.daysLateOn(ISSUED.plusDays(14))).isZero();
        assertThat(loan.daysLateOn(ISSUED.plusDays(17))).isEqualTo(3);
    }

    @Test
    void renewalExtendsFromCurrentDueDate() {
        Loan loan = loan();
        loan.renew(14);
        assertThat(loan.getDueDate()).isEqualTo(ISSUED.plusDays(28));
        assertThat(loan.getRenewalCount()).isEqualTo(1);
    }

    @Test
    void returnedLoanIsNeverOverdue() {
        Loan loan = loan();
        loan.markReturned(Instant.parse("2026-12-01T05:00:00Z"), 2L);
        assertThat(loan.isActive()).isFalse();
        assertThat(loan.isOverdueOn(ISSUED.plusDays(40))).isFalse();
    }

    @Test
    void fineIsDaysTimesRate() {
        Fine fine = new Fine(loan(), 6, new BigDecimal("5.00"), Instant.now());
        assertThat(fine.expectedAmount()).isEqualByComparingTo("30.00");
    }
}
