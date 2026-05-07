package com.tnh.baseware.core.dtos.adu;

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
public class OrganizationDTO extends RepresentationModel<OrganizationDTO> implements Identifiable<UUID> {

    UUID id;
    String name;
    String code;
    String countryCode;
    String provinceCode;
    String communeCode;
    String address;
    String phone;
    String email;
    String website;
    String description;
    Double latitude;
    Double longitude;
    Integer level;

    OrganizationDTO parent;
    List<OrganizationDTO> children;
    Boolean isUnit;
}
