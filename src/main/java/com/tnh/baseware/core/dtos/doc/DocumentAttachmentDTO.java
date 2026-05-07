package com.tnh.baseware.core.dtos.doc;

import com.tnh.baseware.core.dtos.basic.BasicDocumentDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentAttachmentDTO extends RepresentationModel<DocumentAttachmentDTO> implements Identifiable<UUID> {
    UUID id;
    BasicDocumentDTO document;
    FileDocumentDTO file;
    Boolean original;
    Double size;
}
