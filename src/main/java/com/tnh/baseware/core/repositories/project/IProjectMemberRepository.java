package com.tnh.baseware.core.repositories.project;

import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.repositories.IGenericRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface IProjectMemberRepository extends IGenericRepository<ProjectMember, UUID> {

    Boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    @Query("""
            SELECT pm.user
            FROM ProjectMember pm
            WHERE pm.project.id = :projectId
            """)
    Set<User> findUsersByProjectId(UUID projectId);

    @Query("""
            SELECT pm.project
            FROM ProjectMember pm
            WHERE pm.user.id = :userId
            """)
    List<Project> findProjectsByUserId(UUID userId);

    List<ProjectMember> findDistinctByProject_Id(UUID projectId);

    List<ProjectMember> findDistinctByUser_Id(UUID userId);

}
