package com.tnh.baseware.core.forms.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class ProjectAttachmentEditorForm {

    UUID projectId;

    @NotNull(message = "{file-documents.not.null}")
    @Schema(description = "Values are retrieved from 'file-documents'")
    UUID fileDocumentId;

    String description;

}
