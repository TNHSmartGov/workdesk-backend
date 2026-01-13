package com.tnh.baseware.core.dtos.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tnh.baseware.core.entities.audit.Identifiable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskCategoryDTO extends RepresentationModel<TaskCategoryDTO> implements Identifiable<UUID> {

    UUID id;
    String name;
    String code;
    Integer orderIndex;
    String description;

    TaskCategoryDTO parent;
    List<TaskCategoryDTO> children;
}
