package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ITaskRequirementRepository extends IGenericRepository<TaskRequirement, UUID> {
    List<TaskRequirement> findByTaskId(UUID taskId);
    Boolean existsByTaskId(UUID taskId);
}
