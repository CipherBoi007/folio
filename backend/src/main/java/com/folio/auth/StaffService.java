package com.folio.auth;

import com.folio.activity.ActivityAction;
import com.folio.activity.ActivityService;
import com.folio.auth.AuthDtos.CreateStaffRequest;
import com.folio.auth.AuthDtos.StaffResponse;
import com.folio.auth.AuthDtos.UpdateStaffRequest;
import com.folio.auth.session.RevocationReason;
import com.folio.auth.session.SessionService;
import com.folio.common.error.ApiException;
import com.folio.common.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService {

    private final StaffUserRepository users;
    private final PasswordEncoder encoder;
    private final SessionService sessions;
    private final ActivityService activity;
    private final Clock clock;

    public StaffService(StaffUserRepository users, PasswordEncoder encoder, SessionService sessions, ActivityService activity, Clock clock) {
        this.users = users;
        this.encoder = encoder;
        this.sessions = sessions;
        this.activity = activity;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> list() {
        return users.findAll(Sort.by("name")).stream().map(StaffResponse::from).toList();
    }

    @Transactional
    public StaffResponse create(CreateStaffRequest request, AuthenticatedUser actor) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.findByEmail(email).isPresent()) {
            throw ApiException.field(ErrorCode.DUPLICATE_USER_EMAIL, "email", "A staff account already uses " + email + ".");
        }
        requireStrong(request.temporaryPassword(), "temporaryPassword");
        StaffUser user = new StaffUser(request.name().trim(), email, encoder.encode(request.temporaryPassword()), request.role());
        user.changePassword(user.getPasswordHash(), true, Instant.now(clock));
        users.saveAndFlush(user);
        activity.record(actor, ActivityAction.STAFF_CREATED, "STAFF", user.getId(),
                "Created %s account for %s".formatted(label(user.getRole()), user.getName()), Map.of("role", user.getRole().name()));
        return StaffResponse.from(user);
    }

    @Transactional
    public StaffResponse update(long id, UpdateStaffRequest request, AuthenticatedUser actor) {
        StaffUser user = load(id);
        boolean self = user.getId().equals(actor.id());
        if (self && (request.role() != user.getRole() || !request.active())) {
            throw new ApiException(ErrorCode.SELF_MODIFICATION);
        }
        boolean deactivating = user.isActive() && !request.active();
        user.update(request.name().trim(), request.role(), request.active());
        if (deactivating) {
            sessions.revokeAll(id, RevocationReason.ACCOUNT_DISABLED);
        }
        activity.record(actor, ActivityAction.STAFF_UPDATED, "STAFF", id,
                "Updated %s (%s, %s)".formatted(user.getName(), label(user.getRole()), user.isActive() ? "active" : "deactivated"), Map.of());
        return StaffResponse.from(user);
    }

    @Transactional
    public void resetPassword(long id, String temporaryPassword, AuthenticatedUser actor) {
        StaffUser user = load(id);
        requireStrong(temporaryPassword, "temporaryPassword");
        user.changePassword(encoder.encode(temporaryPassword), true, Instant.now(clock));
        sessions.revokeAll(id, RevocationReason.PASSWORD_CHANGED);
        activity.record(actor, ActivityAction.PASSWORD_RESET, "STAFF", id,
                "Reset the password for %s".formatted(user.getName()), Map.of());
    }

    @Transactional
    public void changeOwnPassword(AuthenticatedUser actor, String current, String next) {
        StaffUser user = load(actor.id());
        if (!encoder.matches(current, user.getPasswordHash())) {
            throw ApiException.field(ErrorCode.INVALID_PASSWORD, "currentPassword", ErrorCode.INVALID_PASSWORD.defaultMessage());
        }
        if (encoder.matches(next, user.getPasswordHash())) {
            throw ApiException.field(ErrorCode.VALIDATION_FAILED, "newPassword", "Choose a password different from the current one.");
        }
        requireStrong(next, "newPassword");
        user.changePassword(encoder.encode(next), false, Instant.now(clock));
        sessions.revokeOthers(actor.id(), actor.sessionId(), RevocationReason.PASSWORD_CHANGED);
        activity.record(actor, ActivityAction.PASSWORD_CHANGED, "STAFF", actor.id(), "Changed their password", Map.of());
    }

    private StaffUser load(long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound(ErrorCode.USER_NOT_FOUND, id));
    }

    private static void requireStrong(String password, String field) {
        if (password.length() < 8 || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw ApiException.field(ErrorCode.VALIDATION_FAILED, field, "Use at least 8 characters with letters and numbers.");
        }
    }

    private static String label(Role role) {
        return role == Role.ADMIN ? "admin" : "librarian";
    }
}
