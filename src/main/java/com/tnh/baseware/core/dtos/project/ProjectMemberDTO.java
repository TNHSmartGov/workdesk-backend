package com.tnh.baseware.core.dtos.project;

import com.tnh.baseware.core.dtos.user.UserDTO;
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
public class ProjectMemberDTO extends RepresentationModel<ProjectMemberDTO> implements Identifiable<UUID> {
    UUID id;
    ProjectDTO project;
    UserDTO user;
    String role;
    Instant joinedAt;
}
