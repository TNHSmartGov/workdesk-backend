package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskMemberRepository
        extends IGenericRepository<TaskMember, UUID> {

    Optional<TaskMember> findByTask_IdAndUser_Id(UUID taskId, UUID userId);

    List<TaskMember> findByTask_Id(UUID taskId);

    List<TaskMember> findByTask(Task task);

    boolean existsByTask_IdAndUser_Id(UUID taskId, UUID userId);

    boolean existsByTask_IdAndRole(UUID taskId, TaskMemberRole role);
}
