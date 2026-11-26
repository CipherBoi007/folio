package com.folio.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.folio.support.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** R7 end to end: replaying the ledger reproduces every book's stock, and loans agree with availability. */
class LedgerConsistencyIT extends AbstractIntegrationTest {

    @Test
    void ledgerSumsMatchCurrentQuantities() {
        List<Map<String, Object>> mismatches = jdbc.queryForList("""
                SELECT b.id, b.title, b.total_quantity, b.available_quantity,
                       coalesce(sum(t.quantity_change) FILTER (WHERE t.type NOT IN ('ISSUE', 'RETURN')), 0) AS ledger_total,
                       coalesce(sum(t.quantity_change), 0) AS ledger_available
                  FROM books b
                  LEFT JOIN inventory_transactions t ON t.book_id = b.id
                 GROUP BY b.id
                HAVING b.total_quantity <> coalesce(sum(t.quantity_change) FILTER (WHERE t.type NOT IN ('ISSUE', 'RETURN')), 0)
                    OR b.available_quantity <> coalesce(sum(t.quantity_change), 0)
                """);
        assertThat(mismatches).isEmpty();
    }

    @Test
    void copiesOnLoanEqualActiveLoans() {
        List<Map<String, Object>> mismatches = jdbc.queryForList("""
                SELECT b.id, b.title
                  FROM books b
                  LEFT JOIN loans l ON l.book_id = b.id AND l.returned_at IS NULL
                 GROUP BY b.id
                HAVING b.total_quantity - b.available_quantity <> count(l.id)
                """);
        assertThat(mismatches).isEmpty();
    }

    @Test
    void ledgerSnapshotsChainWithoutGaps() {
        Integer broken = jdbc.queryForObject("""
                SELECT count(*) FROM (
                    SELECT available_before, lag(available_after) OVER (PARTITION BY book_id ORDER BY performed_at, id) AS previous
                      FROM inventory_transactions) chained
                 WHERE previous IS NOT NULL AND previous <> available_before
                """, Integer.class);
        assertThat(broken).isZero();
    }

    @Test
    void demoLibraryIsLoaded() {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM books", Integer.class)).isGreaterThanOrEqualTo(40);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM members", Integer.class)).isGreaterThanOrEqualTo(25);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM loans WHERE returned_at IS NULL", Integer.class)).isGreaterThanOrEqualTo(15);
    }
}
