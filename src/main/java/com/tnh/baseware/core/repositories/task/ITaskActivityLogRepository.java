package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.TaskActivityLog;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ITaskActivityLogRepository extends IGenericRepository<TaskActivityLog, UUID> {
    List<TaskActivityLog> findByTaskId(UUID taskId);

    @org.springframework.data.jpa.repository.Query("""
                SELECT log FROM TaskActivityLog log
                JOIN log.task t
                LEFT JOIN t.project p
                WHERE
                (
                    (t.project IS NULL OR p.organization.id = :orgId)
                    AND
                    (
                        (p IS NOT NULL AND EXISTS (SELECT 1 FROM ProjectMember pm WHERE pm.project.id = p.id AND pm.user.id = :userId))
                        OR
                        EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
                    )
                )
                ORDER BY log.createdDate DESC
            """)
    org.springframework.data.domain.Page<TaskActivityLog> findAccessibleByUser(
            @org.springframework.data.repository.query.Param("orgId") UUID orgId,
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            org.springframework.data.domain.Pageable pageable);
}
