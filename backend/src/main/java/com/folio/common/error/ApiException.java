package com.folio.common.error;

import java.util.List;
import java.util.Map;

/** Expected failure raised by services. The message is shown to staff verbatim, so it says what happened and what to do. */
public class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final transient Map<String, Object> details;
    private final transient List<FieldError> fieldErrors;

    public ApiException(ErrorCode code) {
        this(code, code.defaultMessage(), Map.of());
    }

    public ApiException(ErrorCode code, String message) {
        this(code, message, Map.of());
    }

    public ApiException(ErrorCode code, String message, Map<String, Object> details) {
        this(code, message, details, List.of());
    }

    private ApiException(ErrorCode code, String message, Map<String, Object> details, List<FieldError> fieldErrors) {
        super(message, null, false, false);
        this.code = code;
        this.details = Map.copyOf(details);
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public static ApiException notFound(ErrorCode code, Object id) {
        return new ApiException(code, code.defaultMessage(), Map.of("id", String.valueOf(id)));
    }

    public static ApiException field(ErrorCode code, String field, String message) {
        return new ApiException(code, message, Map.of(), List.of(new FieldError(field, message)));
    }

    public ErrorCode code() {
        return code;
    }

    public Map<String, Object> details() {
        return details;
    }

    public List<FieldError> fieldErrors() {
        return fieldErrors;
    }
}
