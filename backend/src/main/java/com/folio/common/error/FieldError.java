package com.folio.common.error;

/** One invalid input field, keyed by the request property path (e.g. {@code isbn}, {@code address.city}). */
public record FieldError(String field, String message) {
}
