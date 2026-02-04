package com.tnh.baseware.core.forms.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserProfileForm {

    @NotBlank(message = "{first.name.not.blank}")
    String firstName;

    @NotBlank(message = "{last.name.not.blank}")
    String lastName;
    String fullName;

    @NotBlank(message = "{phone.not.blank}")
    String phone;

    String description;

    @NotBlank(message = "{email.not.blank}")
    String email;
    String avatarUrl;
    Instant birthday;
    String address;
    @NotBlank(message = "{idn.not.blank}")
    String idn;

    @NotNull(message = "{ial.not.null}")
    @Schema(description = "0: level 0 identity account, 1: level 1 identity account, 2: level 2 identity account", defaultValue = "0")
    Integer ial;
}
