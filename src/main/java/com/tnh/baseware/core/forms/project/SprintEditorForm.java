package com.tnh.baseware.core.forms.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.enums.project.SprintStatus;
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
public class SprintEditorForm {

    @NotBlank(message = "{name.not.blank}")
    String name;

    Integer orderIndex;

    String goal;
    Instant startDate;
    Instant endDate;

    @NotNull(message = "{project.id.not.null}")
    UUID projectId;

    SprintStatus status;
}
