package com.folio.common.error;

import org.springframework.http.HttpStatus;

/**
 * Every machine-readable error the API can return. The frontend switches on {@link #name()};
 * humans read the accompanying message, which services phrase specifically for each case.
 */
public enum ErrorCode {

    // 400 — request is malformed or fails validation
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Some fields are invalid. Fix them and try again."),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "The request body could not be read."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "A request parameter is invalid."),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "That sort field is not supported."),
    INVALID_ISBN(HttpStatus.BAD_REQUEST, "That ISBN is not valid. Check the digits and try again."),

    // 401 / 403
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Sign in to continue."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Your access token has expired."),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "Your session has ended. Sign in again."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email or password is incorrect."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You don't have permission to do that."),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "That book does not exist."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "That member does not exist."),
    LOAN_NOT_FOUND(HttpStatus.NOT_FOUND, "That loan does not exist."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "That staff account does not exist."),
    FINE_NOT_FOUND(HttpStatus.NOT_FOUND, "That fine does not exist."),

    // 405 / 415
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "That method is not allowed on this resource."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "That content type is not supported."),

    // 409 — business rule violations (CLAUDE.md §4)
    DUPLICATE_ISBN(HttpStatus.CONFLICT, "A book with this ISBN already exists."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "A member with this email already exists."),
    STOCK_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "Available copies cannot exceed total copies."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "Not enough copies on the shelf for this change."),
    BORROW_LIMIT_REACHED(HttpStatus.CONFLICT, "This member already has the maximum number of books on loan."),
    MEMBER_INACTIVE(HttpStatus.CONFLICT, "This member is not active."),
    HAS_OVERDUE_LOANS(HttpStatus.CONFLICT, "This member has overdue loans."),
    FINE_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "This member has unpaid fines above the limit."),
    ALREADY_BORROWED(HttpStatus.CONFLICT, "This member already has a copy of this book."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "No copies of this book are available."),
    ALREADY_RETURNED(HttpStatus.CONFLICT, "This loan has already been returned."),
    LOAN_OVERDUE(HttpStatus.CONFLICT, "Overdue loans cannot be renewed."),
    RENEWAL_LIMIT_REACHED(HttpStatus.CONFLICT, "This loan has already been renewed."),
    BOOK_RESERVED(HttpStatus.CONFLICT, "Another member is waiting for this book."),
    BOOK_AVAILABLE(HttpStatus.CONFLICT, "Copies are on the shelf, so this book can be issued straight away."),
    ALREADY_WAITLISTED(HttpStatus.CONFLICT, "This member is already waiting for this book."),
    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT, "A staff account with this email already exists."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "The current password is incorrect."),
    SELF_MODIFICATION(HttpStatus.CONFLICT, "You can't change your own role or deactivate your own account."),
    BOOK_HAS_ACTIVE_LOANS(HttpStatus.CONFLICT, "This book has copies on loan."),
    MEMBER_HAS_ACTIVE_LOANS(HttpStatus.CONFLICT, "This member has books on loan."),
    MEMBER_HAS_UNPAID_FINES(HttpStatus.CONFLICT, "This member has unpaid fines."),
    FINE_ALREADY_RESOLVED(HttpStatus.CONFLICT, "This fine has already been settled."),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "Someone else changed this record at the same time. Refresh and try again."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "The change conflicts with existing data."),

    // 429
    TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "Too many failed sign-in attempts. Try again in 15 minutes."),

    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our side. Try again in a moment.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
