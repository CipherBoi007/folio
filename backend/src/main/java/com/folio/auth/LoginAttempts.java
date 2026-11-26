package com.folio.auth;

import com.folio.common.config.FolioProperties;
import java.time.Clock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Persists sign-in outcomes in their own transactions so a rejected login still records the failure. */
@Component
class LoginAttempts {

    private final StaffUserRepository users;
    private final FolioProperties.Auth config;
    private final Clock clock;

    LoginAttempts(StaffUserRepository users, FolioProperties properties, Clock clock) {
        this.users = users;
        this.config = properties.auth();
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(long userId) {
        users.recordFailedLogin(userId, config.maxFailedLogins(), clock.instant().plus(config.lockoutDuration()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(long userId) {
        users.recordSuccessfulLogin(userId, clock.instant());
    }
}
