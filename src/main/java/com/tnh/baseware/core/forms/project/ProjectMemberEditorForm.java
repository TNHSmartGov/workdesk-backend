package com.tnh.baseware.core.forms.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class ProjectMemberEditorForm {

    @NotNull(message = "{project_id.not.null}")
    UUID projectId;

    @NotNull(message = "{user_id.not.null}")
    UUID userId;

    @NotNull(message = "{role.not.null}")
    String role;
}
