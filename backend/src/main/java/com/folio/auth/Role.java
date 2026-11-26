package com.folio.auth;

/** Staff roles. ADMIN = head librarian (destructive and financial-override actions); LIBRARIAN = desk staff. */
public enum Role {
    ADMIN,
    LIBRARIAN;

    public String authority() {
        return "ROLE_" + name();
    }
}
