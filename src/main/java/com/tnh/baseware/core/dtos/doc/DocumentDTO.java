package com.tnh.baseware.core.dtos.doc;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentDTO extends RepresentationModel<DocumentDTO> implements Identifiable<UUID> {

    UUID id;
    String documentNumber;
    String summary;
    Instant issuedDate;
    String issuedBy;
    String documentType;
    String externalSource;
    String externalId;
    String documentForm;
}
