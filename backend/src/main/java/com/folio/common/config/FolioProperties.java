package com.folio.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "folio")
public record FolioProperties(
        @NotBlank String timeZone,
        @Valid @NotNull Auth auth,
        @Valid @NotNull Cors cors) {

    public record Auth(
            @NotBlank @Size(min = 32, message = "must be at least 32 characters for HS256") String jwtSecret,
            @NotBlank String jwtIssuer,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl,
            @NotNull Duration sessionMaxAge,
            @NotNull Duration refreshReuseGrace,
            @Min(1) int maxFailedLogins,
            @NotNull Duration lockoutDuration,
            @Valid @NotNull Cookie cookie) {
    }

    public record Cookie(@NotBlank String name, @NotBlank String path, boolean secure, @NotBlank String sameSite) {
    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {
    }
}
