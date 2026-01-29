package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ITaskMemberRepository
    extends IGenericRepository<TaskMember, UUID> {

  Optional<TaskMember> findByTask_IdAndUser_Id(UUID taskId, UUID userId);

  List<TaskMember> findByTask_Id(UUID taskId);

  List<TaskMember> findByTask(Task task);

  boolean existsByTask_IdAndUser_Id(UUID taskId, UUID userId);

  boolean existsByTask_IdAndRole(UUID taskId, TaskMemberRole role);

  @Query("""
      SELECT new com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO(
          u.id, u.fullName, u.avatarUrl,
          COUNT(t),
          SUM(CASE WHEN t.dueDate < :now THEN 1L ELSE 0L END)
      )
      FROM TaskMember tm
      JOIN tm.user u
      JOIN tm.task t
      JOIN t.project p
      WHERE p.organization.id = :orgId
        AND tm.role IN :roles
        AND t.status != :doneStatus
        AND t.status != 'CANCELLED'
      GROUP BY u.id, u.fullName, u.avatarUrl
      """)
  List<com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO> getWorkloadDistribution(
      @Param("orgId") UUID orgId,
      @Param("roles") List<TaskMemberRole> roles,
      @Param("now") java.time.Instant now,
      @Param("doneStatus") com.tnh.baseware.core.enums.task.TaskStatus doneStatus);
}
