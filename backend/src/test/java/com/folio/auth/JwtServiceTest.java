package com.folio.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.folio.common.config.FolioProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-0123456789abcdef-0123456789";
    private static final Instant NOW = Instant.parse("2026-11-26T04:30:00Z");
    private static final AuthenticatedUser ADMIN =
            new AuthenticatedUser(1L, "admin@folio.dev", "Meenakshi Sundaram", Role.ADMIN, UUID.randomUUID());

    private static JwtService service(String secret, String issuer, Instant now) {
        var auth = new FolioProperties.Auth(secret, issuer, Duration.ofMinutes(15), Duration.ofDays(7), Duration.ofDays(30),
                Duration.ofSeconds(30), 5, Duration.ofMinutes(15), new FolioProperties.Cookie("folio_rt", "/api/v1/auth", true, "Strict"));
        var props = new FolioProperties("Asia/Kolkata", auth, new FolioProperties.Cors(List.of("http://localhost:3000")));
        return new JwtService(props, Clock.fixed(now, ZoneId.of("Asia/Kolkata")));
    }

    @Test
    void roundTripsUserAndSession() {
        JwtService jwt = service(SECRET, "folio-api", NOW);
        JwtService.AccessToken token = jwt.issue(ADMIN);

        assertThat(token.expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
        assertThat(jwt.verify(token.value())).isEqualTo(ADMIN);
    }

    @Test
    void expiredTokenIsReportedAsExpired() {
        String token = service(SECRET, "folio-api", NOW).issue(ADMIN).value();
        JwtService later = service(SECRET, "folio-api", NOW.plus(Duration.ofMinutes(16)));

        assertThatThrownBy(() -> later.verify(token))
                .isInstanceOfSatisfying(JwtService.InvalidTokenException.class, e -> assertThat(e.isExpired()).isTrue());
    }

    @Test
    void rejectsForeignSignatureAndIssuer() {
        String forged = service("another-secret-0123456789abcdef-0123456789", "folio-api", NOW).issue(ADMIN).value();
        String foreign = service(SECRET, "someone-else", NOW).issue(ADMIN).value();
        JwtService jwt = service(SECRET, "folio-api", NOW);

        assertThatThrownBy(() -> jwt.verify(forged))
                .isInstanceOfSatisfying(JwtService.InvalidTokenException.class, e -> assertThat(e.isExpired()).isFalse());
        assertThatThrownBy(() -> jwt.verify(foreign)).isInstanceOf(JwtService.InvalidTokenException.class);
    }

    @Test
    void rejectsTamperedRole() {
        JwtService jwt = service(SECRET, "folio-api", NOW);
        var desk = new AuthenticatedUser(2L, "desk@folio.dev", "Karthik Raja", Role.LIBRARIAN, UUID.randomUUID());
        String[] parts = jwt.issue(desk).value().split("\\.");
        String elevated = Base64.getUrlEncoder().withoutPadding().encodeToString(
                new String(Base64.getUrlDecoder().decode(parts[1])).replace("LIBRARIAN", "ADMIN").getBytes());

        assertThatThrownBy(() -> jwt.verify(parts[0] + "." + elevated + "." + parts[2]))
                .isInstanceOf(JwtService.InvalidTokenException.class);
        assertThatThrownBy(() -> jwt.verify("not-a-jwt")).isInstanceOf(JwtService.InvalidTokenException.class);
    }
}
