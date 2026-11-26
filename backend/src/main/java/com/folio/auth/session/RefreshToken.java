package com.folio.auth.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, updatable = false)
    private UserSession session;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "token_hash", nullable = false, updatable = false, length = 64, columnDefinition = "char(64)")
    private String tokenHash;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "rotated_at")
    private Instant rotatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id")
    private RefreshToken replacedBy;

    protected RefreshToken() {
    }

    RefreshToken(UserSession session, String tokenHash, Instant issuedAt, Instant expiresAt) {
        this.session = session;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    boolean isRotated() {
        return rotatedAt != null;
    }

    boolean isExpiredAt(Instant now) {
        return !expiresAt.isAfter(now);
    }

    void rotateTo(RefreshToken successor, Instant now) {
        this.rotatedAt = now;
        this.replacedBy = successor;
    }

    public Long getId() {
        return id;
    }

    public UserSession getSession() {
        return session;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRotatedAt() {
        return rotatedAt;
    }
}
