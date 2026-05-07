package com.tnh.baseware.core.configs;

import com.tnh.baseware.core.entities.project.ProjectRole;
import com.tnh.baseware.core.entities.project.ProjectRolePermission;
import com.tnh.baseware.core.enums.project.ProjectMemberRole;
import com.tnh.baseware.core.enums.project.ProjectPermission;
import com.tnh.baseware.core.repositories.project.IProjectRolePermissionRepository;
import com.tnh.baseware.core.repositories.project.IProjectRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class ProjectRoleSeeder implements ApplicationRunner {

    private final IProjectRoleRepository projectRoleRepository;
    private final IProjectRolePermissionRepository projectRolePermissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (ProjectMemberRole memberRole : ProjectMemberRole.values()) {
            seedRole(
                    memberRole.getValue(),
                    memberRole.getDisplayName(),
                    memberRole.getOrder(),
                    memberRole.getPermissions());
        }
        log.info("Project roles seeding completed.");
    }

    private void seedRole(String code, String displayName, int order, Set<ProjectPermission> permissions) {
        if (projectRoleRepository.existsByCode(code)) {
            log.debug("Project role '{}' already exists, skipping.", code);
            return;
        }

        ProjectRole role = ProjectRole.builder()
                .code(code)
                .name(displayName)
                .description(displayName)
                .order(order)
                .active(true)
                .build();
        role = projectRoleRepository.save(role);

        List<ProjectRolePermission> rolePermissions = new ArrayList<>();
        for (ProjectPermission permission : permissions) {
            rolePermissions.add(ProjectRolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .build());
        }
        projectRolePermissionRepository.saveAll(rolePermissions);
        log.info("Seeded project role '{}' with {} permissions.", code, permissions.size());
    }
}
