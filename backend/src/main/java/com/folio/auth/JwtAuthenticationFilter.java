package com.folio.auth;

import com.folio.auth.session.SessionService;
import com.folio.common.error.ErrorCode;
import com.folio.common.error.GlobalExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates bearer tokens. Besides the signature, the token's session must still be live in the database,
 * so signing out or disabling an account takes effect immediately rather than when the token expires.
 */
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final SessionService sessionService;

    JwtAuthenticationFilter(JwtService jwtService, SessionService sessionService) {
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, BEARER, 0, BEARER.length())) {
            authenticate(request, header.substring(BEARER.length()).trim());
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("userId");
        }
    }

    private void authenticate(HttpServletRequest request, String token) {
        AuthenticatedUser user;
        try {
            user = jwtService.verify(token);
        } catch (JwtService.InvalidTokenException e) {
            reject(request, e.isExpired() ? ErrorCode.TOKEN_EXPIRED : ErrorCode.UNAUTHORIZED);
            return;
        }
        if (!sessionService.isActive(user.sessionId(), user.id())) {
            reject(request, ErrorCode.SESSION_EXPIRED);
            return;
        }
        var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(user.role().authority())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        MDC.put("userId", String.valueOf(user.id()));
    }

    private static void reject(HttpServletRequest request, ErrorCode code) {
        SecurityContextHolder.clearContext();
        request.setAttribute(GlobalExceptionHandler.AUTH_ERROR_ATTRIBUTE, code);
    }
}
