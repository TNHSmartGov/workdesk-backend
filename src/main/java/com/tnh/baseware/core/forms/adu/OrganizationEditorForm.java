package com.tnh.baseware.core.forms.adu;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class OrganizationEditorForm {

    @NotBlank(message = "{name.not.blank}")
    String name;

    @NotBlank(message = "{code.not.blank}")
    String code;
    String countryCode;
    String provinceCode;
    String communeCode;
    String address;
    String phone;
    String email;
    String website;
    String description;
    Double latitude;
    Double longitude;
    Boolean isUnit;

    @NotNull(message = "{level.not.null}")
    @Schema(description = "Values are retrieved from 'organizations/enums?name=OrganizationLevel'")
    Integer level;
}
