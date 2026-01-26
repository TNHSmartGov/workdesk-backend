package com.tnh.baseware.core.dtos.task;

import com.tnh.baseware.core.enums.task.TaskPriority;
import com.tnh.baseware.core.enums.task.TaskStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CalendarTaskDTO {
    UUID id;
    String title;
    Instant startDate;
    Instant dueDate;
    TaskStatus status;
    TaskPriority priority;
    UUID projectId;
    String projectName;

    // Frontend can derive color from Status/Priority, but we can send a hint if
    // needed.
}
