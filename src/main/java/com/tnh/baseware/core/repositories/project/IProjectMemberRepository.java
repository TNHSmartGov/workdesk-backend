package com.tnh.baseware.core.repositories.project;

import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IProjectMemberRepository extends IGenericRepository<ProjectMember, UUID> {
}
