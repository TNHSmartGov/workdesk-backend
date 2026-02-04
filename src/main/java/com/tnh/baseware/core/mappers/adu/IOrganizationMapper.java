package com.tnh.baseware.core.mappers.adu;

import com.tnh.baseware.core.dtos.adu.OrganizationDTO;
import com.tnh.baseware.core.dtos.basic.BasicOrganizationDTO;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.forms.adu.OrganizationEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IOrganizationMapper extends IGenericMapper<Organization, OrganizationEditorForm, OrganizationDTO> {

    @Mapping(source = "parent", target = "parent", qualifiedByName = "mapParent")
    OrganizationDTO entityToDTO(Organization entity);

    BasicOrganizationDTO entityToBasicDTO(Organization entity);

    @Named("mapParent")
    default OrganizationDTO mapParent(Organization parent) {
        return parent == null ? null
                : OrganizationDTO.builder()
                        .id(parent.getId())
                        .name(parent.getName())
                        .code(parent.getCode())
                        .countryCode(parent.getCountryCode())
                        .provinceCode(parent.getProvinceCode())
                        .communeCode(parent.getCommuneCode())
                        .address(parent.getAddress())
                        .phone(parent.getPhone())
                        .email(parent.getEmail())
                        .website(parent.getWebsite())
                        .description(parent.getDescription())
                        .latitude(parent.getLatitude())
                        .longitude(parent.getLongitude())
                        .isUnit(parent.getIsUnit())
                        .level(parent.getLevel())
                        .build();
    }

    default List<OrganizationDTO> mapOrganizationsToTree(List<Organization> organizations) {
        if (organizations == null || organizations.isEmpty())
            return List.of();

        var parentMap = organizations.stream()
                .filter(o -> o.getParent() != null)
                .collect(Collectors.groupingBy(o -> o.getParent().getId()));

        return organizations.stream()
                .filter(o -> o.getParent() == null)
                .map(o -> buildOrganizationTree(o, parentMap))
                .toList();
    }

    default OrganizationDTO buildOrganizationTree(Organization o, Map<UUID, List<Organization>> parentMap) {
        var dto = entityToDTO(o);
        var children = parentMap.getOrDefault(o.getId(), List.of());

        if (!children.isEmpty()) {
            var childDTOs = children.stream()
                    .map(child -> buildOrganizationTree(child, parentMap))
                    .toList();
            dto.setChildren(childDTOs);
        }

        return dto;
    }
}
