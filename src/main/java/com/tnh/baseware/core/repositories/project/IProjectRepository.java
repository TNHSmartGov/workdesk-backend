package com.tnh.baseware.core.repositories.project;

import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

  Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);

  Page<Project> findByOrganizationId(UUID organizationId, Pageable pageable);

  List<Project> findByOrganizationId(UUID organizationId, Sort sort);

  @Query("""
      SELECT COUNT(DISTINCT p)
      FROM Project p
      JOIN ProjectMember pm ON pm.project = p
      WHERE pm.user.id = :userId
        AND p.organization.id = :orgId
        AND p.status = 'ACTIVE'
      """)
  long countActiveProjectsByUser(@org.springframework.data.repository.query.Param("orgId") UUID orgId,
      @org.springframework.data.repository.query.Param("userId") UUID userId);

  List<Project> findAllByOrganizationId(UUID organizationId);
}
