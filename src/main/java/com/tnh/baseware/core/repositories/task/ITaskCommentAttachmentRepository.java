package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskCommentAttachment;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ITaskCommentAttachmentRepository extends IGenericRepository<TaskCommentAttachment, UUID> {
    long countByComment_Id(UUID taskCommentId);

    List<TaskCommentAttachment> findByComment_Id(UUID taskCommentId);
}
