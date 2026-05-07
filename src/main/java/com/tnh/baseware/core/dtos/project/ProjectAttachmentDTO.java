package com.tnh.baseware.core.dtos.project;

import com.tnh.baseware.core.dtos.basic.BasicProjectDTO;
import com.tnh.baseware.core.dtos.basic.BasicUserDTO;
import com.tnh.baseware.core.dtos.doc.FileDocumentDTO;
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
public class ProjectAttachmentDTO extends RepresentationModel<ProjectAttachmentDTO> implements Identifiable<UUID> {
    UUID id;
    BasicProjectDTO project;
    BasicUserDTO uploader;
    FileDocumentDTO file;
    String description;
}
