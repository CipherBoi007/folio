package com.folio.auth;

import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Cookie-authenticated endpoints (refresh, logout) accept browser requests only from configured origins.
 * Together with SameSite=Strict this closes cross-site request forgery against the refresh cookie.
 */
@Component
class OriginGuard {

    private final CorsConfigurationSource corsSource;

    OriginGuard(CorsConfigurationSource corsSource) {
        this.corsSource = corsSource;
    }

    void check(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null) {
            return;
        }
        CorsConfiguration config = corsSource.getCorsConfiguration(request);
        if (config == null || config.checkOrigin(origin) == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Requests from this origin are not allowed.");
        }
    }
}
