package com.tnh.baseware.core.forms.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class TokenEditorForm {

    String jti;
    UUID sessionId;
    String tokenType;
    String deviceId;
    String device;
    String ipAddress;
    String platform;
    String browser;
    Boolean revoked;
    Boolean expired;
    Instant expiration;
}
