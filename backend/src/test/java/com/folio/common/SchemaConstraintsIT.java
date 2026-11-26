package com.folio.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.folio.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/** The database enforces the brief's rules even if application code is bypassed. Each test rolls back. */
@Transactional
class SchemaConstraintsIT extends AbstractIntegrationTest {

    private long insertBook(String isbn, int total, int available) {
        return jdbc.queryForObject("""
                INSERT INTO books (isbn13, title, genre_id, total_quantity, available_quantity)
                VALUES (?, 'Test Title', (SELECT min(id) FROM genres), ?, ?) RETURNING id
                """, Long.class, isbn, total, available);
    }

    private long adminId() {
        return jdbc.queryForObject("SELECT id FROM users WHERE email = 'admin@folio.dev'", Long.class);
    }

    @Test
    void r1_isbnIsUniqueAndWellFormed() {
        String isbn = newIsbn();
        insertBook(isbn, 1, 1);
        assertViolates(() -> insertBook(isbn, 1, 1), "uq_books_isbn13");
    }

    @Test
    void r1_rejectsNonIsbn13() {
        assertViolates(() -> insertBook("1234567890123", 1, 1), "ck_books_isbn13_format");
    }

    @Test
    void r2_availableCannotExceedTotal() {
        assertViolates(() -> insertBook(newIsbn(), 2, 3), "ck_books_available_le_total");
    }

    @Test
    void r3_stockCannotGoNegative() {
        long id = insertBook(newIsbn(), 1, 0);
        assertThatThrownBy(() -> jdbc.update("UPDATE books SET available_quantity = -1 WHERE id = ?", id))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_books_available_nonneg");
    }

    @Test
    void r4_oneActiveLoanPerMemberAndTitle() {
        long book = insertBook(newIsbn(), 2, 2);
        long member = jdbc.queryForObject("SELECT min(id) FROM members", Long.class);
        String insert = "INSERT INTO loans (book_id, member_id, issued_at, due_date, issued_by) VALUES (?, ?, now(), current_date + 14, ?)";
        jdbc.update(insert, book, member, adminId());
        assertViolates(() -> jdbc.update(insert, book, member, adminId()), "uq_loans_active_member_book");
    }

    @Test
    void r7_ledgerRowsMustAddUp() {
        long book = insertBook(newIsbn(), 0, 0);
        assertViolates(() -> jdbc.update("""
                INSERT INTO inventory_transactions (book_id, type, quantity_change, total_before, total_after,
                    available_before, available_after, reason, performed_by, performed_at)
                VALUES (?, 'STOCK_IN', 2, 0, 3, 0, 3, 'Purchase', ?, now())
                """, book, adminId()), "ck_inv_available_delta");
    }

    @Test
    void r7_ledgerCannotBeEdited() {
        assertThatThrownBy(() -> jdbc.update("UPDATE inventory_transactions SET note = 'edited'"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("append-only");
    }

    @Test
    void r7_ledgerCannotBeDeleted() {
        assertThatThrownBy(() -> jdbc.update("DELETE FROM inventory_transactions"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("append-only");
    }

    @Test
    void fineAmountIsDerivedFromDaysAndRate() {
        Long loan = jdbc.queryForObject("SELECT min(l.id) FROM loans l LEFT JOIN fines f ON f.loan_id = l.id WHERE f.id IS NULL", Long.class);
        jdbc.update("INSERT INTO fines (loan_id, overdue_days, daily_rate, created_at) VALUES (?, 7, 5.00, now())", loan);
        assertThat(jdbc.queryForObject("SELECT amount FROM fines WHERE loan_id = ?", java.math.BigDecimal.class, loan))
                .isEqualByComparingTo("35.00");
    }

    private static void assertViolates(Runnable action, String constraint) {
        assertThatThrownBy(action::run).isInstanceOf(DataIntegrityViolationException.class).hasMessageContaining(constraint);
    }
}
