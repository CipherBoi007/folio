package com.folio.auth;

import com.folio.auth.AuthDtos.LoginRequest;
import com.folio.auth.AuthDtos.SessionResponse;
import com.folio.auth.AuthDtos.TokenResponse;
import com.folio.auth.AuthDtos.UserResponse;
import com.folio.auth.session.ClientInfo;
import com.folio.auth.session.RevocationReason;
import com.folio.auth.session.SessionService;
import com.folio.auth.session.SessionService.IssuedRefreshToken;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final StaffUserRepository users;
    private final LoginAttempts attempts;
    private final SessionService sessions;
    private final JwtService jwtService;
    private final Clock clock;

    public AuthService(AuthenticationManager authenticationManager, StaffUserRepository users, LoginAttempts attempts,
                       SessionService sessions, JwtService jwtService, Clock clock) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.attempts = attempts;
        this.sessions = sessions;
        this.jwtService = jwtService;
        this.clock = clock;
    }

    public SignedIn login(LoginRequest request, ClientInfo client) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Optional<StaffUser> account = users.findByEmail(email);
        if (account.isPresent() && account.get().isLockedAt(clock.instant())) {
            throw new ApiException(ErrorCode.TOO_MANY_ATTEMPTS);
        }
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        } catch (AuthenticationException e) {
            account.ifPresent(user -> attempts.recordFailure(user.getId()));
            log.info("Failed sign-in for {}", email);
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        StaffUser user = account.orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));
        attempts.recordSuccess(user.getId());
        SessionService.OpenedSession session = sessions.open(user, client);
        log.info("User {} signed in", user.getId());
        return signedIn(user, session.sessionId(), session.refreshToken());
    }

    public SignedIn refresh(String refreshToken, ClientInfo client) {
        SessionService.Rotation rotation = sessions.rotate(refreshToken, client);
        return signedIn(rotation.user(), rotation.sessionId(), rotation.refreshToken());
    }

    public void logout(String refreshToken, AuthenticatedUser principal) {
        if (refreshToken != null) {
            sessions.revokeByToken(refreshToken);
        }
        if (principal != null) {
            sessions.revoke(principal.sessionId(), RevocationReason.LOGOUT);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse me(AuthenticatedUser principal) {
        return users.findById(principal.id())
                .filter(StaffUser::isActive)
                .map(UserResponse::from)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }

    public List<SessionResponse> sessions(AuthenticatedUser principal) {
        return sessions.activeSessions(principal.id()).stream()
                .map(session -> SessionResponse.from(session, principal.sessionId()))
                .toList();
    }

    public void revokeSession(AuthenticatedUser principal, UUID sessionId) {
        sessions.revokeOwned(principal.id(), sessionId);
    }

    private SignedIn signedIn(StaffUser user, UUID sessionId, IssuedRefreshToken refreshToken) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(), user.getName(), user.getRole(), sessionId);
        JwtService.AccessToken access = jwtService.issue(principal);
        return new SignedIn(new TokenResponse(access.value(), access.expiresAt(), UserResponse.from(user)), refreshToken);
    }

    /** {@code refreshToken} is null when the existing cookie should be left as it is. */
    public record SignedIn(TokenResponse body, IssuedRefreshToken refreshToken) {
    }
}
