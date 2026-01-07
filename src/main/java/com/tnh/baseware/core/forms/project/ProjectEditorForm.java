package com.tnh.baseware.core.forms.project;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.constants.Views;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class ProjectEditorForm {
    @JsonView(Views.Common.class)
    String name;

    @JsonView(Views.Common.class)
    String code;

    @JsonView(Views.Common.class)
    String description;

    @JsonView(Views.Common.class)
    UUID organizationId;

    @JsonView(Views.Common.class)
    Instant startDate;

    @JsonView(Views.Common.class)
    Instant endDate;

    @JsonView(Views.Create.class)
    boolean createDefaultTaskList;

    @JsonView(Views.Create.class)
    String defaultTaskListName;
}
