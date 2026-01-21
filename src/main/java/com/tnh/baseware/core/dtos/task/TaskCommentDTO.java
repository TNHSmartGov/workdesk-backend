package com.tnh.baseware.core.dtos.task;

import com.tnh.baseware.core.dtos.basic.BasicTaskDTO;
import com.tnh.baseware.core.dtos.basic.BasicUserDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskCommentDTO extends RepresentationModel<TaskCommentDTO> implements Identifiable<UUID> {

    UUID id;
    BasicTaskDTO task;
    BasicUserDTO user;
    String content;
    Instant createdDate;
    UUID parentId;
    List<TaskCommentDTO> replies;
    Long replyCount;
    Long attachmentCount;
}
