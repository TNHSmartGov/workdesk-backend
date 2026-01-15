package com.tnh.baseware.core.forms.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class TaskMemberEditorForm {

    @NotNull(message = "{task.id.not.null}")
    UUID taskId;

    @NotNull(message = "{user.id.not.null}")
    UUID userId;

    @NotNull(message = "{role.not.null}")
    @Schema(description = "Values are retrieved from 'task-members/enums?name=TaskMemberRole'")
    TaskMemberRole role;

    @Min(value = 1, message = "{weight.min}")
    @Max(value = 100, message = "{weight.max}")
    @Builder.Default
    Integer weight = 1;
}
