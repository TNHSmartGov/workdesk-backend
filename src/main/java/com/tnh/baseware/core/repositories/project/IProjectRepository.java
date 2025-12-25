package com.tnh.baseware.core.repositories.project;

import com.tnh.baseware.core.entities.audit.Category;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IProjectRepository extends IGenericRepository<Project, UUID> {
    Optional<Project> findByCode(String code);

    @Query("""
            SELECT p
            FROM Project p
            JOIN ProjectMember pm ON pm.project = p
            WHERE p.type = 'PERSONAL'
              AND pm.user.id = :userId
              AND pm.role = 'OWNER'
            """)
    Optional<Project> findPersonalByUser(UUID userId);
}
