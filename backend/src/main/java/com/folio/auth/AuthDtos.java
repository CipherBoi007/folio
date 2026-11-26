package com.folio.auth;

import com.folio.auth.session.UserSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "Enter your email address.")
            @Email(message = "Enter a valid email address.")
            @Size(max = 254, message = "Email is too long.")
            String email,
            @NotBlank(message = "Enter your password.")
            @Size(max = 128, message = "Password is too long.")
            @JsonDeserialize(using = StringDeserializer.class)
            String password) {
    }

    public record UserResponse(Long id, String name, String email, Role role, boolean mustChangePassword) {

        static UserResponse from(StaffUser user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isMustChangePassword());
        }
    }

    public record StaffResponse(Long id, String name, String email, Role role, boolean active, boolean mustChangePassword, Instant lastLoginAt) {

        static StaffResponse from(StaffUser u) {
            return new StaffResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive(), u.isMustChangePassword(), u.getLastLoginAt());
        }
    }

    public record CreateStaffRequest(
            @NotBlank(message = "Enter a name.") @Size(max = 120) String name,
            @NotBlank(message = "Enter an email address.") @Email(message = "Enter a valid email address.") @Size(max = 254) String email,
            @NotNull(message = "Choose a role.") Role role,
            @NotBlank @Size(min = 8, max = 128, message = "Use 8 to 128 characters.") @JsonDeserialize(using = StringDeserializer.class) String temporaryPassword) {
    }

    public record UpdateStaffRequest(
            @NotBlank(message = "Enter a name.") @Size(max = 120) String name,
            @NotNull(message = "Choose a role.") Role role,
            @NotNull Boolean active) {
    }

    public record ResetPasswordRequest(
            @NotBlank @Size(min = 8, max = 128, message = "Use 8 to 128 characters.") @JsonDeserialize(using = StringDeserializer.class) String temporaryPassword) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "Enter your current password.") @JsonDeserialize(using = StringDeserializer.class) String currentPassword,
            @NotBlank @Size(min = 8, max = 128, message = "Use 8 to 128 characters.") @JsonDeserialize(using = StringDeserializer.class) String newPassword) {
    }

    public record TokenResponse(String accessToken, Instant expiresAt, UserResponse user) {
    }

    public record SessionResponse(
            UUID id, Instant createdAt, Instant lastUsedAt, Instant expiresAt, String ipAddress, String userAgent, boolean current) {

        static SessionResponse from(UserSession session, UUID currentSessionId) {
            return new SessionResponse(session.getId(), session.getCreatedAt(), session.getLastUsedAt(), session.getExpiresAt(),
                    session.getIpAddress(), session.getUserAgent(), session.getId().equals(currentSessionId));
        }
    }
}
