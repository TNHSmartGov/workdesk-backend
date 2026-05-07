package com.tnh.baseware.core.forms.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProjectRoleEditorForm {

    @NotBlank(message = "{code.not.blank}")
    String code;

    @NotBlank(message = "{name.not.blank}")
    String name;

    String description;

    int order;

    @NotNull(message = "{permissions.not.null}")
    Set<String> permissions;
}
