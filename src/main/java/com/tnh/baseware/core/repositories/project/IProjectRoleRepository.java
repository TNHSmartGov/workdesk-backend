package com.tnh.baseware.core.repositories.project;

import com.tnh.baseware.core.entities.project.ProjectRole;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IProjectRoleRepository extends IGenericRepository<ProjectRole, UUID> {

    Optional<ProjectRole> findByCode(String code);

    List<ProjectRole> findByActiveTrueOrderByOrderAsc();

    boolean existsByCode(String code);
}
