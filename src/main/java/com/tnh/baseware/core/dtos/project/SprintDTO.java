package com.tnh.baseware.core.dtos.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.project.SprintStatus;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SprintDTO extends RepresentationModel<SprintDTO> implements Identifiable<UUID> {

    UUID id;
    Integer orderIndex;
    String name;
    String goal;
    Instant startDate;
    Instant endDate;
    Instant completedDate;
    SprintStatus status;
    UUID projectId;

    // Statistics
    Integer totalStoryPoints;
    Integer completedStoryPoints;
    Integer progress; // 0-100
}
