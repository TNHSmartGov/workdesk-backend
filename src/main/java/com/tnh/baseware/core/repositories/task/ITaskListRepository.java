package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskListRepository extends IGenericRepository<TaskList, UUID> {
    boolean existsByProjectId(UUID projectId);

    Optional<TaskList> findDefaultByProjectId(UUID projectId);

    List<TaskList> findByProjectIdOrderByOrderIndexAsc(UUID projectId);
}
