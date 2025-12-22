package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskActivityLog;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ITaskActivityLogRepository extends IGenericRepository<TaskActivityLog, UUID> {
}
