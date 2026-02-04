package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskAgileInfo;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskAgileInfoRepository extends IGenericRepository<TaskAgileInfo, UUID> {

    Optional<TaskAgileInfo> findByTaskId(UUID taskId);
}
