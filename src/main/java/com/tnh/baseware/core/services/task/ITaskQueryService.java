package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskDTO;

import com.tnh.baseware.core.dtos.task.TaskTimelineItemDTO;

import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.specs.SearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ITaskQueryService extends ITaskService {

    List<TaskDTO> findByProjectId(UUID projectId);

    Page<TaskDTO> findByProjectId(UUID projectId, Pageable pageable);

    List<TaskDTO> findByTaskListId(UUID taskListId);

    Page<TaskDTO> findByTaskListId(UUID taskListId, Pageable pageable);

    List<TaskDTO> findAccessibleByUser();

    Page<TaskDTO> findAccessibleByUser(Pageable pageable);

    List<TaskDTO> findByStatus(TaskStatus status);

    Page<TaskDTO> findByStatus(TaskStatus status, Pageable pageable);

    Page<TaskDTO> searchTasksCreatedByMe(SearchRequest searchRequest);

    Page<TaskDTO> searchTasksAssignedToMe(SearchRequest searchRequest);

    Page<TaskDTO> searchAccessibleByUser(SearchRequest searchRequest);

    Page<TaskDTO> searchByProjectId(UUID projectId, SearchRequest searchRequest);

    Page<TaskDTO> searchByTaskListId(UUID taskListId, SearchRequest searchRequest);

    Page<TaskDTO> searchByStatus(TaskStatus status, SearchRequest searchRequest);

    List<TaskTimelineItemDTO> getTaskTimeline(UUID taskId);

    List<TaskDTO> getTasksViewingForUser(UUID userId, Instant from, Instant to);

}
