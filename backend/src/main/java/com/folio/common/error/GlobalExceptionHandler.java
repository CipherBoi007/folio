package com.folio.common.error;

import com.folio.common.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Turns every failure into an RFC 7807 {@link ProblemDetail} with Folio's extra members:
 * {@code code}, {@code message} (human, shown verbatim in the UI), {@code fieldErrors}, {@code timestamp}, {@code requestId}.
 * Stack traces are logged for 5xx and never returned.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Request attribute set by the JWT filter when a presented token is expired (vs. missing or invalid). */
    public static final String AUTH_ERROR_ATTRIBUTE = "folio.authError";

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    // ---------------------------------------------------------------- domain

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApi(ApiException ex, WebRequest request) {
        if (ex.code().status().is5xxServerError()) {
            log.error("Server error {}: {}", ex.code(), ex.getMessage(), ex);
        } else {
            log.info("Request rejected {}: {}", ex.code(), ex.getMessage());
        }
        return respond(problem(ex.code(), ex.getMessage(), ex.fieldErrors(), ex.details(), request));
    }

    // -------------------------------------------------------------- security

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex, WebRequest request) {
        ErrorCode code = request.getAttribute(AUTH_ERROR_ATTRIBUTE, WebRequest.SCOPE_REQUEST) instanceof ErrorCode c
                ? c
                : ErrorCode.UNAUTHORIZED;
        return respond(problem(code, code.defaultMessage(), List.of(), Map.of(), request));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.info("Access denied: {}", ex.getMessage());
        return respond(problem(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage(), List.of(), Map.of(), request));
    }

    // ------------------------------------------------------------- database

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        String constraint = constraintName(ex);
        ErrorCode code = ConstraintViolationMapper.resolve(constraint).orElse(ErrorCode.DATA_CONFLICT);
        log.warn("Database constraint {} rejected a write; mapped to {}", constraint, code);
        return respond(problem(code, code.defaultMessage(), List.of(), Map.of(), request));
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Object> handleConcurrency(ConcurrencyFailureException ex, WebRequest request) {
        log.warn("Concurrent modification: {}", ex.getMessage());
        ErrorCode code = ErrorCode.CONCURRENT_MODIFICATION;
        return respond(problem(code, code.defaultMessage(), List.of(), Map.of(), request));
    }

    // ------------------------------------------------------------ validation

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Object> handleBeanValidation(jakarta.validation.ConstraintViolationException ex, WebRequest request) {
        List<FieldError> errors = ex.getConstraintViolations().stream()
                .map(v -> new FieldError(lastPathNode(v.getPropertyPath().toString()), v.getMessage()))
                .sorted(Comparator.comparing(FieldError::field))
                .toList();
        return respond(problem(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.defaultMessage(), errors, Map.of(), request));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldError(e.getField(), messageOf(e)))
                .sorted(Comparator.comparing(FieldError::field))
                .toList();
        return respond(problem(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.defaultMessage(), errors, Map.of(), request));
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldError(result.getMethodParameter().getParameterName(), messageOf(error))))
                .sorted(Comparator.comparing(FieldError::field))
                .toList();
        return respond(problem(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.defaultMessage(), errors, Map.of(), request));
    }

    // -------------------------------------------- framework (404, 405, 415…)

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ErrorCode code = frameworkCode(ex, statusCode);
        String message = code == ErrorCode.INVALID_PARAMETER && body instanceof ProblemDetail pd && pd.getDetail() != null
                ? pd.getDetail()
                : code.defaultMessage();
        if (statusCode.is5xxServerError()) {
            log.error("Unhandled framework error", ex);
        }
        ProblemDetail problem = problem(code, message, List.of(), Map.of(), request);
        problem.setStatus(statusCode.value());
        return ResponseEntity.status(statusCode).headers(headers).body(problem);
    }

    // ------------------------------------------------------------- fallback

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        return respond(problem(code, code.defaultMessage(), List.of(), Map.of(), request));
    }

    // -------------------------------------------------------------- helpers

    private ProblemDetail problem(ErrorCode code, String message, List<FieldError> fieldErrors,
                                  Map<String, Object> details, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(code.status(), message);
        problem.setType(URI.create("urn:folio:problem:" + code.name().toLowerCase(Locale.ROOT).replace('_', '-')));
        problem.setTitle(code.status().getReasonPhrase());
        HttpServletRequest servletRequest = request instanceof NativeWebRequest nwr
                ? nwr.getNativeRequest(HttpServletRequest.class)
                : null;
        if (servletRequest != null) {
            problem.setInstance(URI.create(servletRequest.getRequestURI()));
        }
        problem.setProperty("code", code.name());
        problem.setProperty("message", message);
        problem.setProperty("fieldErrors", fieldErrors);
        problem.setProperty("timestamp", Instant.now(clock));
        problem.setProperty("requestId", requestId(servletRequest));
        if (!details.isEmpty()) {
            problem.setProperty("details", details);
        }
        return problem;
    }

    private static ResponseEntity<Object> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private static String requestId(HttpServletRequest request) {
        Object fromRequest = request == null ? null : request.getAttribute(RequestIdFilter.ATTRIBUTE);
        return fromRequest != null ? fromRequest.toString() : MDC.get(RequestIdFilter.MDC_KEY);
    }

    private static ErrorCode frameworkCode(Exception ex, HttpStatusCode status) {
        if (ex instanceof NoResourceFoundException) {
            return ErrorCode.NOT_FOUND;
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return ErrorCode.METHOD_NOT_ALLOWED;
        }
        if (ex instanceof HttpMediaTypeNotSupportedException) {
            return ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return ErrorCode.MALFORMED_REQUEST;
        }
        return switch (HttpStatus.valueOf(status.value())) {
            case BAD_REQUEST -> ErrorCode.INVALID_PARAMETER;
            case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case METHOD_NOT_ALLOWED -> ErrorCode.METHOD_NOT_ALLOWED;
            case UNSUPPORTED_MEDIA_TYPE -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
            default -> status.is4xxClientError() ? ErrorCode.INVALID_PARAMETER : ErrorCode.INTERNAL_ERROR;
        };
    }

    private static String constraintName(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof org.hibernate.exception.ConstraintViolationException hibernate) {
                return hibernate.getConstraintName();
            }
        }
        return null;
    }

    private static String messageOf(MessageSourceResolvable error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : "is invalid";
    }

    private static String lastPathNode(String path) {
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : path;
    }
}
