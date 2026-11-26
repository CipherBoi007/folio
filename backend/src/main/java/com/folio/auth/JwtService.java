package com.folio.auth;

import com.folio.common.config.FolioProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_SESSION = "sid";
    private static final long CLOCK_SKEW_SECONDS = 30;

    private final FolioProperties.Auth config;
    private final SecretKey key;
    private final Clock clock;

    public JwtService(FolioProperties properties, Clock clock) {
        this.config = properties.auth();
        this.key = Keys.hmacShaKeyFor(config.jwtSecret().getBytes(StandardCharsets.UTF_8));
        this.clock = clock;
    }

    public AccessToken issue(AuthenticatedUser user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(config.accessTokenTtl());
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(config.jwtIssuer())
                .subject(String.valueOf(user.id()))
                .claim(CLAIM_EMAIL, user.email())
                .claim(CLAIM_NAME, user.name())
                .claim(CLAIM_ROLE, user.role().name())
                .claim(CLAIM_SESSION, user.sessionId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new AccessToken(token, expiresAt);
    }

    public AuthenticatedUser verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(config.jwtIssuer())
                    .clock(() -> Date.from(clock.instant()))
                    .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new AuthenticatedUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get(CLAIM_EMAIL, String.class),
                    claims.get(CLAIM_NAME, String.class),
                    Role.valueOf(claims.get(CLAIM_ROLE, String.class)),
                    UUID.fromString(claims.get(CLAIM_SESSION, String.class)));
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(true);
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            throw new InvalidTokenException(false);
        }
    }

    public record AccessToken(String value, Instant expiresAt) {
    }

    public static final class InvalidTokenException extends RuntimeException {

        private final boolean expired;

        InvalidTokenException(boolean expired) {
            super(expired ? "Token expired" : "Token invalid", null, false, false);
            this.expired = expired;
        }

        public boolean isExpired() {
            return expired;
        }
    }
}
