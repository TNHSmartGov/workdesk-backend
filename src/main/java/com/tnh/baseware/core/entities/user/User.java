package com.tnh.baseware.core.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.enums.UserType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends Auditable<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(unique = true, nullable = false)
    String username;

    @Column(nullable = false)
    String password;

    @Column(nullable = false)
    @Builder.Default
    String firstName = "";

    @Column(nullable = false)
    @Builder.Default
    String lastName = "";

    String fullName;

    @Column(unique = true, nullable = false)
    String phone;

    @Column(unique = false)
    String email;
    String avatarUrl;

    @Column(unique = false, nullable = true)
    @Builder.Default
    String idn = "";

    @Column(nullable = false)
    @Builder.Default
    Boolean ignorePayment = Boolean.FALSE; // user can view cams without payment

    @Column(nullable = false)
    @Builder.Default
    Integer ial = 0;

    @Column(nullable = false)
    @Builder.Default
    Boolean enabled = Boolean.TRUE;

    @Column(nullable = false)
    @Builder.Default
    Boolean locked = Boolean.FALSE;

    Instant lockTime;
    Instant accountExpiryDate;
    String address;
    @Column(nullable = false)
    @Builder.Default
    Integer failedLoginAttempts = 0;

    @Column(nullable = false)
    @Builder.Default
    Boolean superAdmin = Boolean.FALSE;

    @Builder.Default
    String userType = UserType.CUSTOMER.getValue();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<Token> tokens = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "users_organizations", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "organization_id", referencedColumnName = "id"))
    @Builder.Default
    Set<Organization> organizations = new HashSet<>();

    public Set<GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(Role::getAuthorities)
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void addRole(Role role) {
        if (role != null) {
            roles.add(role);
            role.getUsers().add(this);
        }
    }
}
