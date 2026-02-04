package com.tnh.baseware.core.repositories.stats;

import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.enums.project.ProjectStatus;
import com.tnh.baseware.core.enums.task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserStatsCalculationRepository extends JpaRepository<TaskMember, UUID> {

    @Query("SELECT COUNT(tm) FROM TaskMember tm WHERE tm.user.id = :userId")
    Integer countTotalTasksByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(tm) FROM TaskMember tm WHERE tm.user.id = :userId")
    Integer countParticipatedTasksByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(tm) FROM TaskMember tm WHERE tm.user.id = :userId AND tm.task.status = :status")
    Integer countCompletedTasksByUserId(@Param("userId") UUID userId, @Param("status") TaskStatus status);

    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.user.id = :userId AND pm.project.status = :status")
    Integer countCompletedProjectsByUserId(@Param("userId") UUID userId, @Param("status") ProjectStatus status);
}
