package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskAttachment;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ITaskAttachmentRepository extends IGenericRepository<TaskAttachment, UUID> {
}
