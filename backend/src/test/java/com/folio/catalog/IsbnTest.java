package com.folio.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class IsbnTest {

    @ParameterizedTest
    @CsvSource({
            "9780143039655, 9780143039655",
            "978-0-14-303965-5, 9780143039655",
            "978 0143 039655, 9780143039655",
            "0143039652, 9780143039655",
            "0-14-303965-2, 9780143039655",
            "080442957X, 9780804429573",
            "080442957x, 9780804429573"
    })
    void normalisesToIsbn13(String raw, String expected) {
        assertThat(Isbn.toIsbn13(raw)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"9780143039656", "0143039653", "12345", "97801430396551", "978014303965X", "X143039652", "1234567890123", ""})
    void rejectsInvalidChecksumsAndShapes(String raw) {
        assertThatThrownBy(() -> Isbn.toIsbn13(raw))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_ISBN));
    }
}
