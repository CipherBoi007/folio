package com.folio.auth.session;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RefreshToken t JOIN FETCH t.session s JOIN FETCH s.user WHERE t.tokenHash = :hash")
    Optional<RefreshToken> findForRotation(@Param("hash") String hash);

    @Query("SELECT t FROM RefreshToken t JOIN FETCH t.session WHERE t.tokenHash = :hash")
    Optional<RefreshToken> findByTokenHash(@Param("hash") String hash);
}
