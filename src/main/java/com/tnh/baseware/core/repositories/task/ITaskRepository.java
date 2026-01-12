package com.tnh.baseware.core.repositories.task;

import com.tnh.baseware.core.dtos.task.UserTaskPermissionDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
