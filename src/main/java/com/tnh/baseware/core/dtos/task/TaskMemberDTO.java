package com.tnh.baseware.core.dtos.task;

import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.task.MemberStatus;
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
public class TaskMemberDTO extends RepresentationModel<TaskMemberDTO> implements Identifiable<UUID> {
    UUID id;
    UUID taskId;
    UUID userId;
    String role;
    MemberStatus status;
    Instant joinedAt;
    Instant completedAt;
}

