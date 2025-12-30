package com.tnh.baseware.core.securities;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.enums.project.ProjectMemberRole;
import com.tnh.baseware.core.enums.project.ProjectPermission;
import com.tnh.baseware.core.repositories.project.IProjectMemberRepository;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectSecurityService {
    IProjectMemberRepository projectMemberRepository;

    public ProjectSecurityService(IProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public boolean checkPermission(UUID userId, UUID projectId, ProjectPermission permission) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElse(null);

        if (member == null) {
            return false;
        }

        ProjectMemberRole role = member.getRole();
        return role.hasPermission(permission);
    }

}
