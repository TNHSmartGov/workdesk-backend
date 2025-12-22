package com.tnh.baseware.core.services.adu;

import com.tnh.baseware.core.dtos.adu.OrganizationDTO;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.enums.CategoryCode;
import com.tnh.baseware.core.forms.adu.OrganizationEditorForm;
import com.tnh.baseware.core.forms.user.AssignUserEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IOrganizationService
        extends IGenericService<Organization, OrganizationEditorForm, OrganizationDTO, UUID> {

    @Override
    List<OrganizationDTO> findAll();

    @Override
    Page<OrganizationDTO> findAll(Pageable pageable);

    void assignOrganizations(UUID id, List<UUID> ids);

    void removeOrganizations(UUID id, List<UUID> ids);

    void assignUsers(UUID id, List<AssignUserEditorForm> forms);

    void removeUsers(UUID id, List<UUID> ids);

    void changeTitle(UUID orgId, UUID userId, UUID titleId);
}
