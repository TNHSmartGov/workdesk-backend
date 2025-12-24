package com.tnh.baseware.core.dtos.project;

import com.tnh.baseware.core.dtos.basic.BasicOrganizationDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.project.ProjectStatus;
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
public class ProjectDTO extends RepresentationModel<ProjectDTO> implements Identifiable<UUID> {
    UUID id;
    String name;
    String code;
    String description;
    BasicOrganizationDTO organization;
    Instant startDate;
    Instant endDate;
    ProjectStatus status;
    List<ProjectAttachmentDTO> attachments;
}
