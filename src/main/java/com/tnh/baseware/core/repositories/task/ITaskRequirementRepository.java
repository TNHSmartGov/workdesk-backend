package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskRequirement;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskRequirementRepository extends IGenericRepository<TaskRequirement, UUID> {
    List<TaskRequirement> findByTaskId(UUID taskId);

    Boolean existsByTaskId(UUID taskId);

    List<TaskRequirement> findByTaskIdOrderBySortOrder(UUID taskId);

    @Query("SELECT MAX(r.sortOrder) FROM TaskRequirement r WHERE r.task.id = :taskId")
    Optional<Integer> findMaxSortOrderByTaskId(@Param("taskId") UUID taskId);
}
