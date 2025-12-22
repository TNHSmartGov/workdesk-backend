package com.tnh.baseware.core.forms.audit;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tnh.baseware.core.enums.CategoryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CategoryEditorForm {

    @NotBlank(message = "{name.not.blank}")
    String name;

    @NotNull(message = "{code.not.null}")
    @Schema(description = "Represents the category code based on the enum 'CategoryCode'")
    CategoryCode code;

    @NotBlank(message = "{display.name.not.blank}")
    String displayName;
    String description;
}
