package com.tnh.baseware.core.repositories.project;

import com.tnh.baseware.core.entities.project.ProjectAttachment;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IProjectAttachmentRepository extends IGenericRepository<ProjectAttachment, UUID> {

    List<ProjectAttachment> findByProjectId(UUID projectId);
}
