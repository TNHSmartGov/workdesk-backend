package com.tnh.baseware.core.forms.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.enums.task.TaskPriority;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.enums.task.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskEditorForm {
    @NotBlank(message = "{title.not.blank}")
    String title;

    String description;

    Instant startDate;
    Instant dueDate;

    @NotNull(message = "{status.not.null}")
    TaskStatus status;

    @NotNull(message = "{priority.not.null}")
    TaskPriority priority;

    @NotNull(message = "{type.not.null}")
    TaskType type;

    @NotNull(message = "{project_id.not.null}")
    UUID projectId;

    @NotNull(message = "{project_id.not.null}")
    UUID taskListId;
}
