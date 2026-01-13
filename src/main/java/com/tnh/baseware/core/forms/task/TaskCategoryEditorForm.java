package com.tnh.baseware.core.forms.task;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class TaskCategoryEditorForm {

    @NotBlank(message = "{name.not.blank}")
    @Schema(description = "Category name", example = "Development Tasks")
    String name;

    @NotBlank(message = "{code.not.blank}")
    @Schema(description = "Unique category code", example = "DEV_TASKS")
    String code;

    @Min(value = 0, message = "{order.index.min}")
    @Schema(description = "Display order index", example = "1")
    Integer orderIndex;

    @Schema(description = "Category description")
    String description;

    @Schema(description = "Parent category ID for tree structure")
    UUID parentId;
}
