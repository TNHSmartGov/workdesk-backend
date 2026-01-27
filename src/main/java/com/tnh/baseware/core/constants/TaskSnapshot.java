package com.tnh.baseware.core.constants;

import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.enums.task.TaskPriority;
import com.tnh.baseware.core.enums.task.TaskType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record TaskSnapshot(
        String title,
        String description,
        Instant startDate,
        Instant dueDate,
        TaskPriority priority,
        TaskType type,
        UUID taskListId,
        UUID taskCategoryId) implements DiffSnapshot {

    public static TaskSnapshot from(Task task) {
        return new TaskSnapshot(
                task.getTitle(),
                task.getDescription(),
                task.getStartDate(),
                task.getDueDate(),
                task.getPriority(),
                task.getType(),
                task.getTaskList() != null ? task.getTaskList().getId() : null,
                task.getTaskCategory() != null ? task.getTaskCategory().getId() : null);
    }

    @Override
    public Map<String, Object> fields() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("startDate", startDate);
        map.put("dueDate", dueDate);
        map.put("priority", priority);
        map.put("type", type);
        map.put("taskList", taskListId);
        map.put("taskCategory", taskCategoryId);
        return map;
    }
}
