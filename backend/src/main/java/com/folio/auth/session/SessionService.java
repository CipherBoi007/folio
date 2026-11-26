package com.folio.auth.session;

import com.folio.auth.StaffUser;
import com.folio.common.config.FolioProperties;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Server-side sessions backed by rotating refresh tokens.
 *
 * <p>Every refresh replaces the presented token. Presenting an already-rotated token after the grace window means
 * the token was copied, so the whole session is revoked. The grace window absorbs two tabs refreshing at once.
 */
@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final UserSessionRepository sessions;
    private final RefreshTokenRepository tokens;
    private final FolioProperties.Auth config;
    private final Clock clock;

    public SessionService(UserSessionRepository sessions, RefreshTokenRepository tokens, FolioProperties properties, Clock clock) {
        this.sessions = sessions;
        this.tokens = tokens;
        this.config = properties.auth();
        this.clock = clock;
    }

    @Transactional
    public OpenedSession open(StaffUser user, ClientInfo client) {
        Instant now = clock.instant();
        UserSession session = sessions.save(new UserSession(user, now, now.plus(config.sessionMaxAge()), client));
        IssuedRefreshToken refresh = issueToken(session, now);
        log.info("Session {} opened for user {}", session.getId(), user.getId());
        return new OpenedSession(session.getId(), refresh);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public Rotation rotate(String rawToken, ClientInfo client) {
        Instant now = clock.instant();
        RefreshToken token = tokens.findForRotation(TokenCodec.hash(rawToken)).orElseThrow(SessionService::sessionEnded);
        UserSession session = token.getSession();
        StaffUser user = session.getUser();

        if (!session.isUsableAt(now)) {
            throw sessionEnded();
        }
        if (!user.isActive()) {
            session.revoke(now, RevocationReason.ACCOUNT_DISABLED);
            throw sessionEnded();
        }
        if (token.isRotated()) {
            if (!now.isAfter(token.getRotatedAt().plus(config.refreshReuseGrace()))) {
                return new Rotation(session.getId(), user, null);
            }
            session.revoke(now, RevocationReason.TOKEN_REUSE);
            log.warn("Refresh token reuse detected; session {} of user {} revoked", session.getId(), user.getId());
            throw sessionEnded();
        }
        if (token.isExpiredAt(now)) {
            throw sessionEnded();
        }

        IssuedRefreshToken next = issueToken(session, now);
        token.rotateTo(next.entity(), now);
        session.touch(now, client);
        return new Rotation(session.getId(), user, next);
    }

    @Transactional
    public void revokeByToken(String rawToken) {
        tokens.findByTokenHash(TokenCodec.hash(rawToken))
                .ifPresent(token -> token.getSession().revoke(clock.instant(), RevocationReason.LOGOUT));
    }

    @Transactional
    public void revoke(UUID sessionId, RevocationReason reason) {
        sessions.findById(sessionId).ifPresent(session -> session.revoke(clock.instant(), reason));
    }

    @Transactional
    public void revokeOwned(long userId, UUID sessionId) {
        UserSession session = sessions.findById(sessionId)
                .filter(s -> s.getUser().getId() == userId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.NOT_FOUND, sessionId));
        session.revoke(clock.instant(), RevocationReason.REVOKED_BY_USER);
    }

    @Transactional
    public int revokeAll(long userId, RevocationReason reason) {
        return sessions.revokeAllForUser(userId, reason, clock.instant());
    }

    /** Signs out every session of the user except {@code keep} (may be null). */
    @Transactional
    public void revokeOthers(long userId, UUID keep, RevocationReason reason) {
        Instant now = clock.instant();
        sessions.findActiveByUser(userId, now).stream()
                .filter(s -> !s.getId().equals(keep))
                .forEach(s -> s.revoke(now, reason));
    }

    @Transactional(readOnly = true)
    public boolean isActive(UUID sessionId, long userId) {
        return sessions.isActive(sessionId, userId, clock.instant());
    }

    @Transactional(readOnly = true)
    public List<UserSession> activeSessions(long userId) {
        return sessions.findActiveByUser(userId, clock.instant());
    }

    @Scheduled(cron = "0 15 3 * * *", zone = "${folio.time-zone}")
    @Transactional
    public void purgeEndedSessions() {
        int removed = sessions.deleteEndedBefore(clock.instant().minus(Duration.ofDays(7)));
        if (removed > 0) {
            log.info("Purged {} ended sessions", removed);
        }
    }

    private IssuedRefreshToken issueToken(UserSession session, Instant now) {
        Instant expiresAt = min(now.plus(config.refreshTokenTtl()), session.getExpiresAt());
        String raw = TokenCodec.newToken();
        RefreshToken entity = tokens.save(new RefreshToken(session, TokenCodec.hash(raw), now, expiresAt));
        return new IssuedRefreshToken(raw, expiresAt, entity);
    }

    private static Instant min(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }

    private static ApiException sessionEnded() {
        return new ApiException(ErrorCode.SESSION_EXPIRED);
    }

    public record IssuedRefreshToken(String value, Instant expiresAt, RefreshToken entity) {
    }

    public record OpenedSession(UUID sessionId, IssuedRefreshToken refreshToken) {
    }

    /** {@code refreshToken} is null when a recently rotated token was replayed inside the grace window. */
    public record Rotation(UUID sessionId, StaffUser user, IssuedRefreshToken refreshToken) {
    }
}
