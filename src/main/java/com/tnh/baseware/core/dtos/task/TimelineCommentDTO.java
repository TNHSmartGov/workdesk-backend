package com.tnh.baseware.core.dtos.task;

import com.tnh.baseware.core.dtos.basic.BasicTaskDTO;
import com.tnh.baseware.core.dtos.basic.BasicUserDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimelineCommentDTO implements Identifiable<UUID> {

    UUID id;
    BasicTaskDTO task;
    BasicUserDTO user;
    String content;
    Instant createdDate;
    UUID parentId;
    List<TimelineCommentDTO> replies;
    Long replyCount;
    Long attachmentCount;
}
