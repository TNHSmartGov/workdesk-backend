package com.tnh.baseware.core.entities.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomUserDetails implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;
    User user;

    @Override
    public Set<? extends GrantedAuthority> getAuthorities() {
        return Optional.ofNullable(user)
                .map(User::getAuthorities)
                .orElse(Collections.emptySet());
    }

    @Override
    public String getPassword() {
        return Optional.ofNullable(user)
                .map(User::getPassword)
                .orElse(null);
    }

    @Override
    public String getUsername() {
        return Optional.ofNullable(user)
                .map(User::getUsername)
                .orElse(null);
    }

    @Override
    public boolean isAccountNonExpired() {
        return Optional.ofNullable(user)
                .map(u -> Instant.now().isBefore(u.getAccountExpiryDate()))
                .orElse(false);
    }

    @Override
    public boolean isAccountNonLocked() {
        return Optional.ofNullable(user)
                .map(u -> !u.getLocked())
                .orElse(false);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Optional.ofNullable(user)
                .map(User::getEnabled)
                .orElse(false);
    }
}
