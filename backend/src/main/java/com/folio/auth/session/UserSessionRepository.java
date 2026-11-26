package com.folio.auth.session;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                  FROM user_sessions s
                  JOIN users u ON u.id = s.user_id
                 WHERE s.id = :sessionId
                   AND s.user_id = :userId
                   AND s.revoked_at IS NULL
                   AND s.expires_at > :now
                   AND u.active)
            """, nativeQuery = true)
    boolean isActive(@Param("sessionId") UUID sessionId, @Param("userId") long userId, @Param("now") Instant now);

    @Query("""
            SELECT s FROM UserSession s
             WHERE s.user.id = :userId AND s.revokedAt IS NULL AND s.expiresAt > :now
             ORDER BY s.lastUsedAt DESC
            """)
    List<UserSession> findActiveByUser(@Param("userId") long userId, @Param("now") Instant now);

    @Modifying
    @Query("""
            UPDATE UserSession s SET s.revokedAt = :now, s.revokedReason = :reason
             WHERE s.user.id = :userId AND s.revokedAt IS NULL
            """)
    int revokeAllForUser(@Param("userId") long userId, @Param("reason") RevocationReason reason, @Param("now") Instant now);

    @Modifying
    @Query(value = "DELETE FROM user_sessions WHERE expires_at < :cutoff OR revoked_at < :cutoff", nativeQuery = true)
    int deleteEndedBefore(@Param("cutoff") Instant cutoff);
}
