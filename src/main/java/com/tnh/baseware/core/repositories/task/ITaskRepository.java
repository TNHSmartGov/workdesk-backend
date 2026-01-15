package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.dtos.task.UserTaskPermissionDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskRepository extends IGenericRepository<Task, UUID> {
    @Query("""
                 SELECT
                     pm.role as projectRole,
                     tm.role as taskRole
                 FROM Task t
                 JOIN ProjectMember pm ON pm.project.id = t.project.id AND pm.user.id = :userId
                 LEFT JOIN TaskMember tm ON tm.task.id = t.id AND tm.user.id = :userId
                 WHERE t.id = :taskId
            """)
    Optional<UserTaskPermissionDTO> findUserPermissions(@Param("taskId") UUID taskId,
                                                        @Param("userId") UUID userId);

    @Query("SELECT t FROM Task t WHERE t.project.organization.id = :orgId")
    List<Task> findByOrganizationId(@Param("orgId") UUID orgId);

    @Query("SELECT t FROM Task t WHERE t.project.organization.id = :orgId")
    Page<Task> findByOrganizationId(@Param("orgId") UUID orgId, Pageable pageable);

    @Query("""
            SELECT DISTINCT t
            FROM Task t
            WHERE t.project.organization.id = :orgId
              AND (
                  EXISTS (
                      SELECT pm
                      FROM ProjectMember pm
                      WHERE pm.project.id = t.project.id
                        AND pm.user.id = :userId
                  )
                  OR EXISTS (
                      SELECT tm
                      FROM TaskMember tm
                      WHERE tm.task.id = t.id
                        AND tm.user.id = :userId
                  )
              )
            """)
    List<Task> findAccessibleByUser(
            @Param("orgId") UUID orgId,
            @Param("userId") UUID userId
    );

    @Query("""
            SELECT DISTINCT t FROM Task t
            WHERE t.project.organization.id = :orgId
            AND (
                EXISTS (SELECT 1 FROM ProjectMember pm WHERE pm.project.id = t.project.id AND pm.user.id = :userId)
                OR EXISTS (SELECT 1 FROM TaskMember tm WHERE tm.task.id = t.id AND tm.user.id = :userId)
            )
            """)
    Page<Task> findAccessibleByUser(@Param("orgId") UUID orgId, @Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND t.project.organization.id = :orgId")
    Optional<Task> findByIdAndOrganizationId(@Param("taskId") UUID taskId, @Param("orgId") UUID orgId);

    List<Task> findByProjectId(UUID projectId);

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    List<Task> findByTaskListId(UUID taskListId);

    Page<Task> findByTaskListId(UUID taskListId, Pageable pageable);
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.project.organization.id = :orgId")
    List<Task> findByStatusAndOrgId(@Param("status") TaskStatus status, @Param("orgId") UUID orgId);
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.project.organization.id = :orgId")
    Page<Task> findByStatusAndOrgId(@Param("status") TaskStatus status, @Param("orgId") UUID orgId, Pageable pageable);

    @Query("""
    SELECT t FROM Task t 
    WHERE t.createdBy = :userId 
    AND t.project.organization.id = :orgId
    ORDER BY t.createdDate DESC
""")
    List<Task> findTasksCreatedByUser(@Param("orgId") UUID orgId, @Param("userId") String userId);
    @Query("""
    SELECT t FROM Task t 
    WHERE t.createdBy = :userId 
    AND t.project.organization.id = :orgId
""")
    Page<Task> findTasksCreatedByUser(@Param("orgId") UUID orgId, @Param("userId") String userId, Pageable pageable);
    @Query("""
    SELECT DISTINCT t FROM Task t 
    JOIN TaskMember tm ON tm.task.id = t.id 
    WHERE tm.user.id = :userId 
    AND tm.role IN ('ASSIGNEE', 'LEAD')
    AND t.project.organization.id = :orgId
    ORDER BY t.createdDate DESC
""")
    List<Task> findTasksAssignedToUser(@Param("orgId") UUID orgId, @Param("userId") UUID userId);
    @Query("""
    SELECT DISTINCT t FROM Task t 
    JOIN TaskMember tm ON tm.task.id = t.id 
    WHERE tm.user.id = :userId 
    AND tm.role IN ('ASSIGNEE', 'LEAD')
    AND t.project.organization.id = :orgId
""")
    Page<Task> findTasksAssignedToUser(@Param("orgId") UUID orgId, @Param("userId") UUID userId, Pageable pageable);

}
