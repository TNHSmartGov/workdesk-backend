package com.tnh.baseware.core.forms.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.enums.task.TaskAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskActionForm {
    @NotNull
    @Schema(description = "Values are retrieved from 'tasks/enums?name=TaskAction'")
    private TaskAction action;

    @Schema(description = "Required when action is REJECT")
    private String reason;

    @Schema(description = "Member IDs to notify for reject/resubmit (optional)")
    private List<UUID> notifyMemberIds;
}
