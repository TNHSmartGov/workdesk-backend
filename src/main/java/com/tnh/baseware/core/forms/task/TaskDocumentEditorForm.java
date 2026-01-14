package com.tnh.baseware.core.forms.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.enums.task.TaskDocumentRelationType;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class TaskDocumentEditorForm {

    @NotNull(message = "{task.id.required}")
    @Schema(description = "Task ID", required = true)
    UUID taskId;

    @NotNull(message = "{document.id.required}")
    @Schema(description = "Document ID", required = true)
    UUID documentId;

    @NotNull(message = "{relation.type.required}")
    @Schema(description = "Relation type between task and document", required = true)
    TaskDocumentRelationType relationType;
}
