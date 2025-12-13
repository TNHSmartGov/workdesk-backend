package com.tnh.baseware.core.forms.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class UserEditorForm {

    @NotBlank(message = "{username.not.blank}")
    String username;

    @NotBlank(message = "{password.not.blank}")
    String password;

    @NotBlank(message = "{first.name.not.blank}")
    String firstName;

    @NotBlank(message = "{last.name.not.blank}")
    String lastName;
    String fullName;

    @NotBlank(message = "{phone.not.blank}")
    String phone;

    String email;
    String avatarUrl;
    String address;
    String idn;
    @NotNull(message = "{ignore.payment.not.null}")
    @Schema(description = "true: user can view cams without payment, false: user must pay to view cams", defaultValue = "false")
    Boolean ignorePayment; // user can view cams without payment
    @NotNull(message = "{ial.not.null}")
    @Schema(description = "0: level 0 identity account, 1: level 1 identity account, 2: level 2 identity account", defaultValue = "0")
    Integer ial;

    @NotNull(message = "{enabled.not.null}")
    @Schema(description = "true: enabled, false: disabled", defaultValue = "true")
    Boolean enabled;
    Instant accountExpiryDate;

    @NotNull(message = "{role.not.null}")
    UUID role;
    @NotNull(message = "{user.type.not.null}")
    @Schema(description = "Values are retrieved from 'users/enums?name=UserType'")
    String userType;
}
