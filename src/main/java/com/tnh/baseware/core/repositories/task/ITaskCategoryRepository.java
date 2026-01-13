package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskCategory;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ITaskCategoryRepository extends IGenericRepository<TaskCategory, UUID> {

    @EntityGraph(attributePaths = { "parent" })
    @Query("SELECT tc FROM TaskCategory tc ORDER BY tc.orderIndex ASC")
    List<TaskCategory> findAllWithParent();
}
