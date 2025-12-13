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
public class TrackActivityDTO extends RepresentationModel<TrackActivityDTO> implements Identifiable<UUID> {

    UUID id;
    String username;
    String requestUrl;
    String method;
    String status;
    String ipAddress;
    String deviceInfo;
    Instant actionDate;
}
