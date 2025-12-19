package com.tnh.baseware.core.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(name = "ApiMessage")
public class ApiMessageDTO<T> {

    @Schema(example = "Operation successful")
    String message;

    @Schema(description = "Payload")
    T data;

    @Schema(example = "true")
    Boolean result;

    @Schema(example = "200")
    Integer code;
}
