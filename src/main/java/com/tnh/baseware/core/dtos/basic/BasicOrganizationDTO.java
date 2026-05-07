package com.tnh.baseware.core.dtos.basic;

import java.util.UUID;

import org.springframework.hateoas.RepresentationModel;

import com.tnh.baseware.core.dtos.adu.OrganizationDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicOrganizationDTO extends RepresentationModel<OrganizationDTO> implements Identifiable<UUID> {
    UUID id;
    String name;
    String code;
}
