package com.tnh.baseware.core.dtos.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tnh.baseware.core.dtos.basic.BasicDocumentDTO;
import com.tnh.baseware.core.dtos.basic.BasicTaskDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.task.TaskDocumentRelationType;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDocumentDTO extends RepresentationModel<TaskDocumentDTO> implements Identifiable<UUID> {
    UUID id;
    BasicTaskDTO task;
    BasicDocumentDTO document;
    TaskDocumentRelationType relationType;
}
