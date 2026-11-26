package com.folio.auth;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {

    Optional<StaffUser> findByEmail(String email);

    /**
     * Counts a failed sign-in atomically. Reaching the limit locks the account and resets the counter,
     * so the user gets a fresh set of attempts once the lock expires.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE users
               SET failed_login_attempts = CASE WHEN failed_login_attempts + 1 >= :maxAttempts THEN 0
                                                ELSE failed_login_attempts + 1 END,
                   locked_until = CASE WHEN failed_login_attempts + 1 >= :maxAttempts THEN :lockUntil
                                       ELSE locked_until END
             WHERE id = :id
            """, nativeQuery = true)
    int recordFailedLogin(@Param("id") long id, @Param("maxAttempts") int maxAttempts, @Param("lockUntil") Instant lockUntil);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE users
               SET failed_login_attempts = 0, locked_until = NULL, last_login_at = :now
             WHERE id = :id
            """, nativeQuery = true)
    int recordSuccessfulLogin(@Param("id") long id, @Param("now") Instant now);
}
