package com.folio.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.folio.common.error.ApiException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MemberServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {"9843012345", "98430 12345", "+91 98430-12345", "919843012345", "09843012345"})
    void normalisesIndianMobileNumbers(String raw) {
        assertThat(MemberService.normalizePhone(raw)).isEqualTo("9843012345");
    }

    @ParameterizedTest
    @ValueSource(strings = {"5843012345", "984301234", "98430123456", "044 2345 6789", "abc"})
    void rejectsNonMobileNumbers(String raw) {
        assertThatThrownBy(() -> MemberService.normalizePhone(raw))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.fieldErrors()).extracting("field").containsExactly("phone"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Priya.Raman@Example.com", "  priya.raman@example.com "})
    void emailsAreStoredLowercase(String raw) {
        assertThat(MemberService.normalizeEmail(raw)).isEqualTo("priya.raman@example.com");
    }
}
