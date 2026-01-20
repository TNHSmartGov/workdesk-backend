package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskComment;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ITaskCommentRepository extends IGenericRepository<TaskComment, UUID> {
    List<TaskComment> findByTaskId(UUID taskId);

    List<TaskComment> findByTaskIdAndParentCommentIsNull(UUID taskId);

    long countByParentComment_Id(UUID parentId);
}
