package com.tnh.baseware.core.repositories.project;

import com.tnh.baseware.core.entities.project.ProjectRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IProjectRolePermissionRepository extends JpaRepository<ProjectRolePermission, UUID> {

    List<ProjectRolePermission> findByRoleId(UUID roleId);

    void deleteByRoleId(UUID roleId);
}
