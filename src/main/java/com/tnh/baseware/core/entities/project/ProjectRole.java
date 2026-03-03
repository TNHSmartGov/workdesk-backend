package com.tnh.baseware.core.entities.project;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.enums.project.ProjectPermission;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
public class ProjectRole extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, unique = true, length = 50)
    String code;

    @Column(nullable = false)
    String name;

    String description;

    @Column(name = "role_order")
    int order;

    @Builder.Default
    boolean active = true;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<ProjectRolePermission> rolePermissions;

    public Set<ProjectPermission> getPermissions() {
        if (rolePermissions == null) {
            return Set.of();
        }
        return rolePermissions.stream()
                .map(ProjectRolePermission::getPermission)
                .collect(Collectors.toSet());
    }

    public boolean hasPermission(ProjectPermission permission) {
        return getPermissions().contains(permission);
    }
}
