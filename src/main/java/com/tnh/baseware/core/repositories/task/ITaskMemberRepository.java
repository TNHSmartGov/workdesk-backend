package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskMemberRepository
        extends IGenericRepository<TaskMember, UUID> {

    Optional<TaskMember> findByTaskIdAndUserId(UUID taskId, UUID userId);

    List<TaskMember> findByTaskId(UUID taskId);
}
