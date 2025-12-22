package com.tnh.baseware.core.forms.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
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
public class TaskRequirementEditorForm {

    @NotNull(message = "{task.id.not.null}")
    UUID taskId;

    @NotBlank(message = "{content.not.blank}")
    String content;

    @NotNull(message = "{is.completed.not.null}")
    Boolean isCompleted;

    @NotNull(message = "{sort.order.not.null}")
    Integer sortOrder;
}
