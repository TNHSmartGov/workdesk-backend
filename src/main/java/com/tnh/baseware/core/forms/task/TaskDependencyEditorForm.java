package com.tnh.baseware.core.forms.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.enums.task.TaskDependencyType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskDependencyEditorForm {

    @NotNull(message = "{from_task_id.not.null}")
    UUID fromTaskId;

    @NotNull(message = "{to_task_id.not.null}")
    UUID toTaskId;

    @NotNull(message = "{dependency_type.not.null}")
    TaskDependencyType dependencyType;
}
