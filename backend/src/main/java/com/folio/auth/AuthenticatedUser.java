package com.folio.auth;

import java.security.Principal;
import java.util.UUID;

public record AuthenticatedUser(Long id, String email, String name, Role role, UUID sessionId) implements Principal {

    @Override
    public String getName() {
        return email;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
