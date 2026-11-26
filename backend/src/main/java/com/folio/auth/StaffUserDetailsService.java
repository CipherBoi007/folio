package com.folio.auth;

import java.util.List;
import java.util.Locale;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Loads staff accounts for password verification at login. Emails are matched case-insensitively. */
@Service
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffUserRepository users;

    public StaffUserDetailsService(StaffUserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        String email = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        StaffUser user = users.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No staff account for " + email));
        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole().authority())))
                .disabled(!user.isActive())
                .build();
    }
}
