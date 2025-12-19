package com.tnh.baseware.core.dtos.audit;

import com.tnh.baseware.core.entities.audit.Identifiable;
import com.tnh.baseware.core.enums.CategoryCode;
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
public class CategoryDTO extends RepresentationModel<CategoryDTO> implements Identifiable<UUID> {

    UUID id;
    CategoryCode code;
    String name;
    String displayName;
    String description;
}
