package com.folio.auth;

import com.folio.common.config.FolioProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
class RefreshCookies {

    private final FolioProperties.Cookie config;
    private final Clock clock;

    RefreshCookies(FolioProperties properties, Clock clock) {
        this.config = properties.auth().cookie();
        this.clock = clock;
    }

    String name() {
        return config.name();
    }

    ResponseCookie issue(String token, Instant expiresAt) {
        return base(token).maxAge(Duration.between(clock.instant(), expiresAt)).build();
    }

    ResponseCookie clear() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(config.name(), value)
                .httpOnly(true)
                .secure(config.secure())
                .sameSite(config.sameSite())
                .path(config.path());
    }
}
