package com.tnh.baseware.core.securities;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.entities.project.ProjectRole;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.entities.user.UserOrganization;
import com.tnh.baseware.core.enums.OrganizationRole;
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
    public boolean checkPermission(User user, Project project, ProjectPermission permission,
            Set<UserOrganization> userOrgs) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getId())
                .orElse(null);

        if (member == null) {
            return false;
        }
        var userOrg = userOrgs.stream()
                .filter(u -> u.getOrganization().getId().equals(project.getOrganization().getId()))
                .findFirst().orElse(null);
        if (userOrg == null) {
            return false;
        }
        if (userOrg.getOrganizationRole() == OrganizationRole.UNIT_LEADER ||
                userOrg.getOrganizationRole() == OrganizationRole.DEPUTY) {
            return true;
        }
        ProjectRole role = member.getProjectRole();
        return role != null && role.hasPermission(permission);
    }

}
