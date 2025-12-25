package com.tnh.baseware.core.dtos.task;

import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.task.TaskPriority;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.enums.task.TaskType;
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
public class TaskDTO extends RepresentationModel<TaskDTO> implements Identifiable<UUID> {
    UUID id;
    String title;
    String description;
    Instant startDate;
    Instant dueDate;
    TaskStatus status;
    TaskPriority priority;
    TaskType type;
    TaskListDTO taskList;
}
