package com.tnh.baseware.core.dtos.project;

import com.tnh.baseware.core.entities.audit.Identifiable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.RepresentationModel;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectRoleDTO extends RepresentationModel<ProjectRoleDTO> implements Identifiable<UUID> {
    UUID id;
    String code;
    String name;
    String description;
    int order;
    boolean active;
    Set<String> permissions;
}
