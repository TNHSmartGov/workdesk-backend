package com.tnh.baseware.core.dtos.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tnh.baseware.core.dtos.project.SprintDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import com.tnh.baseware.core.entities.audit.Identifiable;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskAgileInfoDTO extends RepresentationModel<TaskAgileInfoDTO> implements Identifiable<UUID> {

    UUID taskId;
    SprintDTO sprint;
    Integer storyPoints;
    Double originalEstimate;
    Double remainingEstimate;
    Double completedHours;
    Double boardPosition;

    @Override
    public UUID getId() {
        return taskId;
    }
}
