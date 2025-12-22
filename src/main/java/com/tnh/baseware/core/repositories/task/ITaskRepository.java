package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ITaskRepository extends IGenericRepository<Task, UUID> {
}
