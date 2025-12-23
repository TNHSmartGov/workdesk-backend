package com.tnh.baseware.core.dtos.project;

import com.tnh.baseware.core.dtos.adu.OrganizationDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.project.ProjectStatus;
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
public class ProjectDTO extends RepresentationModel<ProjectDTO> implements Identifiable<UUID> {
    UUID id;
    String name;
    String code;
    String description;
    UUID organizationId;
    String organizationName;
    Instant startDate;
    Instant endDate;
    ProjectStatus status;
}
