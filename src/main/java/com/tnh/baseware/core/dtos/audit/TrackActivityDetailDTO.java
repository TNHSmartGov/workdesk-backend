package com.tnh.baseware.core.dtos.audit;

import com.tnh.baseware.core.entities.audit.Identifiable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackActivityDetailDTO extends RepresentationModel<TrackActivityDetailDTO> implements Identifiable<UUID> {

    UUID id;
    String username;
    String requestUrl;
    String method;
    String status;
    String ipAddress;
    String deviceInfo;
    String requestPayload;
    String responsePayload;
    Instant actionDate;

    public TrackActivityDetailDTO(String requestPayload, String responsePayload) {
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
    }
}
