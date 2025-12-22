package com.tnh.baseware.core.dtos.task;

import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.task.TaskDependencyType;
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
public class TaskDependencyDTO extends RepresentationModel<TaskDependencyDTO> implements Identifiable<UUID> {
    UUID id;
    TaskDTO fromTask;
    TaskDTO toTask;
    TaskDependencyType dependencyType;
}
