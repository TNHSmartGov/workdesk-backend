package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ITaskRequirementRepository extends IGenericRepository<TaskRequirement, UUID> {
}
