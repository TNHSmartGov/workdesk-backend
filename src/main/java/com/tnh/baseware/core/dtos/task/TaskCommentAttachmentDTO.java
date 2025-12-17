package com.tnh.baseware.core.dtos.task;

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
public class TaskCommentAttachmentDTO extends RepresentationModel<TaskCommentAttachmentDTO> implements Identifiable<UUID> {

    UUID id;
    UUID commentId;
    FileDocumentDTO file;
    UUID uploaderId;
}
