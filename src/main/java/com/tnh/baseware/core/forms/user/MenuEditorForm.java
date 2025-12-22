package com.tnh.baseware.core.forms.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class MenuEditorForm {

    @NotBlank(message = "{title.not.blank}")
    String title;

    @NotNull(message = "{menu.type.id.not.null}")
    @Schema(description = "Values are retrieved from 'categories/by-field?fieldName=code&value=menuType'")
    UUID menuTypeId;

    @NotBlank(message = "{alias.not.blank}")
    String alias;

    String note;

    @NotBlank(message = "{path.not.blank}")
    String path;

    @NotBlank(message = "{link.not.blank}")
    String link;

    @NotNull(message = "{published.not.null}")
    Integer published;

    @NotNull(message = "{browser.nav.not.null}")
    Integer browserNav;

    @NotBlank(message = "{icon.not.blank}")
    String icon;

    @NotNull(message = "{menu.order.not.null}")
    Integer menuOrder;
    String description;
}
