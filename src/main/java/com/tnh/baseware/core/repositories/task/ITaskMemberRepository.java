package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskMemberRepository
                extends IGenericRepository<TaskMember, UUID> {

        Optional<TaskMember> findByTask_IdAndUser_Id(UUID taskId, UUID userId);

        List<TaskMember> findByTask_Id(UUID taskId);

        List<TaskMember> findByTask_IdAndRole(UUID taskId, TaskMemberRole role);

        List<TaskMember> findByTask(Task task);

        boolean existsByTask_IdAndUser_Id(UUID taskId, UUID userId);

        boolean existsByTask_IdAndRole(UUID taskId, TaskMemberRole role);

        @Query("""
                        SELECT tm
                        FROM TaskMember tm
                        JOIN tm.task t
                        WHERE tm.role = :role
                          AND tm.deleted = false
                          AND t.deleted = false
                          AND t.status != 'DONE' AND t.status != 'CANCELLED'
                          AND t.dueDate IS NOT NULL
                          AND t.dueDate >= :now AND t.dueDate <= :future
                        """)
        List<TaskMember> findAssigneesDueSoon(@Param("role") TaskMemberRole role,
                        @Param("now") Instant now,
                        @Param("future") Instant future);

        @Query("""
                        SELECT tm
                        FROM TaskMember tm
                        JOIN tm.task t
                        WHERE tm.role = :role
                          AND tm.deleted = false
                          AND t.deleted = false
                          AND t.status != 'DONE' AND t.status != 'CANCELLED'
                          AND t.dueDate IS NOT NULL
                          AND t.dueDate < :now
                        """)
        List<TaskMember> findAssigneesOverdue(@Param("role") TaskMemberRole role,
                        @Param("now") Instant now);

        @Query("""
                        SELECT new com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO(
                            u.id, u.fullName, u.avatarUrl,
                            COUNT(t),
                            COALESCE(SUM(CASE WHEN t.dueDate < :now THEN 1L ELSE 0L END), 0L)
                        )
                        FROM com.tnh.baseware.core.entities.user.UserOrganization uo
                        JOIN uo.user u
                        LEFT JOIN TaskMember tm ON tm.user = u
                            AND tm.role IN :roles
                            AND tm.deleted = false
                        LEFT JOIN tm.task t ON t = tm.task
                            AND t.status != :doneStatus
                            AND t.status != 'CANCELLED'
                            AND t.deleted = false
                        WHERE uo.organization.id = :orgId
                          AND uo.active = true
                        GROUP BY u.id, u.fullName, u.avatarUrl
                        """)
        List<UnitWorkloadDTO> getWorkloadDistribution(
                        @Param("orgId") UUID orgId,
                        @Param("roles") List<TaskMemberRole> roles,
                        @Param("now") java.time.Instant now,
                        @Param("doneStatus") TaskStatus doneStatus);

        @Query("""
                        SELECT new com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO(
                            u.id, u.fullName, u.avatarUrl,
                            COUNT(t),
                            COALESCE(SUM(CASE WHEN t.dueDate < :now THEN 1L ELSE 0L END), 0L)
                        )
                        FROM com.tnh.baseware.core.entities.user.UserOrganization uo
                        JOIN uo.user u
                        LEFT JOIN TaskMember tm ON tm.user = u
                            AND tm.role IN :roles
                            AND tm.deleted = false
                        LEFT JOIN tm.task t ON t = tm.task
                            AND t.status != :doneStatus
                            AND t.status != 'CANCELLED'
                            AND t.deleted = false
                            AND t.createdDate >= :from
                            AND t.createdDate <= :to
                        WHERE uo.organization.id = :orgId
                          AND uo.active = true
                        GROUP BY u.id, u.fullName, u.avatarUrl
                        """)
        List<UnitWorkloadDTO> getWorkloadDistributionTimeboxed(
                        @Param("orgId") UUID orgId,
                        @Param("roles") List<TaskMemberRole> roles,
                        @Param("now") java.time.Instant now,
                        @Param("doneStatus") TaskStatus doneStatus,
                        @Param("from") java.time.Instant from,
                        @Param("to") java.time.Instant to);
}
