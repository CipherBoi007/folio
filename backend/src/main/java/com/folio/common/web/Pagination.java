package com.folio.common.web;

import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Builds a safe {@link Pageable} from raw query parameters. Sort fields are whitelisted per endpoint
 * (public name → entity property) so clients can never sort on arbitrary columns, and page size is capped.
 *
 * <p>Sort syntax: {@code field} or {@code field,asc|desc}.
 */
public final class Pagination {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private Pagination() {
    }

    public static Pageable pageable(Integer page, Integer size, String sort, Map<String, String> allowedSorts, Sort fallback) {
        int safePage = page == null ? 0 : page;
        int safeSize = size == null ? DEFAULT_SIZE : size;
        if (safePage < 0) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "Page must be 0 or greater.");
        }
        if (safeSize < 1 || safeSize > MAX_SIZE) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER, "Page size must be between 1 and " + MAX_SIZE + ".");
        }
        return PageRequest.of(safePage, safeSize, parseSort(sort, allowedSorts, fallback));
    }

    static Sort parseSort(String sort, Map<String, String> allowedSorts, Sort fallback) {
        if (sort == null || sort.isBlank()) {
            return fallback;
        }
        String[] parts = sort.trim().split(",");
        if (parts.length > 2) {
            throw invalidSort(sort);
        }
        String property = allowedSorts.get(parts[0].trim());
        if (property == null) {
            throw invalidSort(parts[0].trim());
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length == 2) {
            direction = switch (parts[1].trim().toLowerCase(Locale.ROOT)) {
                case "asc" -> Sort.Direction.ASC;
                case "desc" -> Sort.Direction.DESC;
                default -> throw invalidSort(sort);
            };
        }
        // Secondary sort on id keeps pagination stable when the primary key has ties.
        return Sort.by(direction, property).and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private static ApiException invalidSort(String value) {
        return new ApiException(ErrorCode.INVALID_SORT, "Sorting by \"" + value + "\" is not supported.");
    }
}
