package com.tnh.baseware.core.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tnh.baseware.core.entities.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
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
public class Role extends Auditable<String> implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        UUID id;

        @Column(unique = true, nullable = false)
        String name;
        String description;

        Boolean isDefault;

        Boolean isSystem;

        @JsonIgnore
        @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
        @Builder.Default
        Set<User> users = new HashSet<>();

        @JsonIgnore
        @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
        @Builder.Default
        Set<Menu> menus = new HashSet<>();

        @JsonIgnore
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "roles_privileges", joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id"))
        @Builder.Default
        Set<Privilege> privileges = new HashSet<>();

        public Set<GrantedAuthority> getAuthorities() {
                return privileges == null ? Set.of()
                                : privileges.stream()
                                                .map(privilege -> new SimpleGrantedAuthority(privilege.getApiPattern()))
                                                .collect(Collectors.toUnmodifiableSet());
        }
}
