package com.tnh.baseware.core.repositories.project;

import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IProjectRepository extends IGenericRepository<Project, UUID> {
}
