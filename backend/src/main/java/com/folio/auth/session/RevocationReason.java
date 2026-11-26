package com.folio.auth.session;

public enum RevocationReason {
    LOGOUT,
    TOKEN_REUSE,
    REVOKED_BY_USER,
    ACCOUNT_DISABLED,
    PASSWORD_CHANGED
}
