package com.tnh.baseware.core.dtos.task;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
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
public class TaskListDTO extends RepresentationModel<TaskListDTO> implements Identifiable<UUID> {
    UUID id;
    String name;
    Integer orderIndex;
    ProjectDTO project;
}
