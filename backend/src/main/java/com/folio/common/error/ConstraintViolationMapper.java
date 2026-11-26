package com.folio.common.error;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Maps named database constraints to API error codes. Services check every rule first; this mapping only
 * fires when a concurrent request slips past the service check and the database becomes the last line of defence.
 * Constraint names are defined in {@code V1__schema.sql} — keep the two in sync.
 */
public final class ConstraintViolationMapper {

    private static final Map<String, ErrorCode> CONSTRAINTS = Map.ofEntries(
            Map.entry("uq_books_isbn13", ErrorCode.DUPLICATE_ISBN),
            Map.entry("ck_books_isbn13_format", ErrorCode.INVALID_ISBN),
            Map.entry("ck_books_available_le_total", ErrorCode.STOCK_LIMIT_EXCEEDED),
            Map.entry("ck_books_available_nonneg", ErrorCode.INSUFFICIENT_STOCK),
            Map.entry("ck_books_total_nonneg", ErrorCode.INSUFFICIENT_STOCK),
            Map.entry("uq_members_email", ErrorCode.DUPLICATE_EMAIL),
            Map.entry("uq_loans_active_member_book", ErrorCode.ALREADY_BORROWED),
            Map.entry("ck_loans_renewal_count", ErrorCode.RENEWAL_LIMIT_REACHED),
            Map.entry("uq_fines_loan", ErrorCode.DATA_CONFLICT),
            Map.entry("uq_waitlist_waiting", ErrorCode.ALREADY_WAITLISTED),
            Map.entry("uq_users_email", ErrorCode.DUPLICATE_USER_EMAIL));

    private ConstraintViolationMapper() {
    }

    public static Optional<ErrorCode> resolve(String constraintName) {
        if (constraintName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(CONSTRAINTS.get(constraintName.toLowerCase(Locale.ROOT)));
    }
}
