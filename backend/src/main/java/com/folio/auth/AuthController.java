package com.folio.auth;

import com.folio.auth.AuthDtos.LoginRequest;
import com.folio.auth.AuthDtos.SessionResponse;
import com.folio.auth.AuthDtos.TokenResponse;
import com.folio.auth.AuthDtos.UserResponse;
import com.folio.auth.session.ClientInfo;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshCookies cookies;
    private final OriginGuard originGuard;

    public AuthController(AuthService authService, RefreshCookies cookies, OriginGuard originGuard) {
        this.authService = authService;
        this.cookies = cookies;
        this.originGuard = originGuard;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Sign in. Returns a short-lived access token and sets the refresh cookie.")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        originGuard.check(request);
        return respond(authService.login(body, ClientInfo.from(request)));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Exchange the refresh cookie for a new access token (rotates the cookie)")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        originGuard.check(request);
        String token = readCookie(request);
        if (token == null) {
            throw new ApiException(ErrorCode.SESSION_EXPIRED);
        }
        try {
            return respond(authService.refresh(token, ClientInfo.from(request)));
        } catch (ApiException e) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookies.clear().toString());
            throw e;
        }
    }

    @PostMapping("/logout")
    @SecurityRequirements
    @Operation(summary = "End the current session")
    public ResponseEntity<Void> logout(HttpServletRequest request, @AuthenticationPrincipal AuthenticatedUser principal) {
        originGuard.check(request);
        authService.logout(readCookie(request), principal);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookies.clear().toString()).build();
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return authService.me(principal);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Active sessions of the signed-in user")
    public List<SessionResponse> sessions(@AuthenticationPrincipal AuthenticatedUser principal) {
        return authService.sessions(principal);
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "Sign out one of your sessions")
    public ResponseEntity<Void> revokeSession(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        authService.revokeSession(principal, id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<TokenResponse> respond(AuthService.SignedIn signedIn) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store");
        if (signedIn.refreshToken() != null) {
            builder.header(HttpHeaders.SET_COOKIE,
                    cookies.issue(signedIn.refreshToken().value(), signedIn.refreshToken().expiresAt()).toString());
        }
        return builder.body(signedIn.body());
    }

    private String readCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (var cookie : request.getCookies()) {
            if (cookie.getName().equals(cookies.name()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
