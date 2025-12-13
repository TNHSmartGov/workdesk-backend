package com.tnh.baseware.core.forms.audit;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
public class TrackActivityEditorForm {

    String username;
    String requestUrl;
    String method;
    Integer status;
    String ipAddress;
    String deviceInfo;
    String requestPayload;
    String responsePayload;
    Instant actionDate;
}
