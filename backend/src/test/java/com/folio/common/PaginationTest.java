package com.folio.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import com.folio.common.web.Pagination;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PaginationTest {

    private static final Map<String, String> SORTS = Map.of("title", "title", "added", "createdAt");
    private static final Sort FALLBACK = Sort.by("title");

    @Test
    void defaultsWhenNothingGiven() {
        Pageable pageable = Pagination.pageable(null, null, null, SORTS, FALLBACK);

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(Pagination.DEFAULT_SIZE);
        assertThat(pageable.getSort()).isEqualTo(FALLBACK);
    }

    @Test
    void mapsWhitelistedPublicNameToPropertyWithStableTiebreak() {
        Pageable pageable = Pagination.pageable(2, 50, "added,desc", SORTS, FALLBACK);

        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")));
    }

    @Test
    void rejectsUnknownSortField() {
        assertThatThrownBy(() -> Pagination.pageable(0, 20, "passwordHash", SORTS, FALLBACK))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_SORT));
    }

    @Test
    void rejectsBadDirection() {
        assertThatThrownBy(() -> Pagination.pageable(0, 20, "title,sideways", SORTS, FALLBACK))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_SORT));
    }

    @Test
    void capsPageSizeAt100() {
        assertThat(Pagination.pageable(0, 100, null, SORTS, FALLBACK).getPageSize()).isEqualTo(100);
        assertThatThrownBy(() -> Pagination.pageable(0, 101, null, SORTS, FALLBACK))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_PARAMETER));
        assertThatThrownBy(() -> Pagination.pageable(-1, 20, null, SORTS, FALLBACK))
                .isInstanceOf(ApiException.class);
    }
}
