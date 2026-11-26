package com.folio.auth.session;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public record ClientInfo(String ipAddress, String userAgent) {

    private static final int MAX_USER_AGENT = 300;
    private static final int MAX_IP = 45;

    public static ClientInfo from(HttpServletRequest request) {
        return new ClientInfo(truncate(request.getRemoteAddr(), MAX_IP), truncate(request.getHeader(HttpHeaders.USER_AGENT), MAX_USER_AGENT));
    }

    private static String truncate(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
