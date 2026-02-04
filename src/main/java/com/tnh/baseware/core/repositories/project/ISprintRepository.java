package com.tnh.baseware.core.repositories.project;

import com.tnh.baseware.core.entities.project.Sprint;
import com.tnh.baseware.core.enums.project.SprintStatus;
import com.tnh.baseware.core.repositories.IGenericRepository;
import com.tnh.baseware.core.repositories.project.projections.SprintStatsProjection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Repository
public interface ISprintRepository extends IGenericRepository<Sprint, UUID> {

        List<Sprint> findAllByProjectId(UUID projectId);

        List<Sprint> findAllByProjectIdAndStatus(UUID projectId, SprintStatus status);

        @Query("SELECT s.id as id, " +
                        "SUM(COALESCE(tai.storyPoints, 0)) as totalStoryPoints, " +
                        "SUM(CASE WHEN t.status = 'COMPLETED' THEN COALESCE(tai.storyPoints, 0) ELSE 0 END) as completedStoryPoints "
                        +
                        "FROM Sprint s " +
                        "LEFT JOIN TaskAgileInfo tai ON tai.sprint.id = s.id " +
                        "LEFT JOIN Task t ON tai.task.id = t.id " +
                        "WHERE s.project.id = :projectId " +
                        "GROUP BY s.id " +
                        "ORDER BY s.orderIndex ASC, s.startDate ASC")
        List<SprintStatsProjection> findAllByProjectIdWithStats(
                        @PathVariable("projectId") UUID projectId);
}
