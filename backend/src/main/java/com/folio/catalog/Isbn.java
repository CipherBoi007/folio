package com.folio.catalog;

import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import java.util.Locale;

/** ISBN normalisation: strips separators, validates the ISBN-10 or ISBN-13 checksum, and stores everything as ISBN-13. */
public final class Isbn {

    private Isbn() {
    }

    public static String toIsbn13(String raw) {
        if (raw == null) {
            throw invalid();
        }
        String digits = raw.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        if (digits.matches("\\d{13}")) {
            if ((digits.startsWith("978") || digits.startsWith("979")) && isValid13(digits)) {
                return digits;
            }
            throw invalid();
        }
        if (digits.matches("\\d{9}[\\dX]") && isValid10(digits)) {
            String core = "978" + digits.substring(0, 9);
            return core + checkDigit13(core);
        }
        throw invalid();
    }

    public static boolean isValid(String raw) {
        try {
            toIsbn13(raw);
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    static boolean isValid13(String isbn) {
        return checkDigit13(isbn.substring(0, 12)) == isbn.charAt(12) - '0';
    }

    static boolean isValid10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char c = isbn.charAt(i);
            int value = c == 'X' ? 10 : c - '0';
            if (c == 'X' && i != 9) {
                return false;
            }
            sum += value * (10 - i);
        }
        return sum % 11 == 0;
    }

    private static int checkDigit13(String first12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = first12.charAt(i) - '0';
            sum += i % 2 == 0 ? digit : digit * 3;
        }
        return (10 - sum % 10) % 10;
    }

    private static ApiException invalid() {
        return new ApiException(ErrorCode.INVALID_ISBN);
    }
}
