package com.tnh.baseware.core.forms.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.enums.task.LogActionType;
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
public class TaskActivityLogEditorForm {

    @NotNull(message = "{task.id.not.null}")
    UUID taskId;

    @NotNull(message = "{actor.id.not.null}")
    UUID actorId;

    @NotNull(message = "{action.type.not.null}")
    LogActionType actionType;

    String targetField;
    String oldValue;
    String newValue;
}
