package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.dtos.project.ProjectRoleDTO;
import com.tnh.baseware.core.entities.project.ProjectRole;
import com.tnh.baseware.core.entities.project.ProjectRolePermission;
import com.tnh.baseware.core.enums.project.ProjectPermission;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.project.ProjectRoleEditorForm;
import com.tnh.baseware.core.mappers.project.IProjectRoleMapper;
import com.tnh.baseware.core.repositories.project.IProjectRolePermissionRepository;
import com.tnh.baseware.core.repositories.project.IProjectRoleRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectRoleService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectRoleService extends
        GenericService<ProjectRole, ProjectRoleEditorForm, ProjectRoleDTO, IProjectRoleRepository, IProjectRoleMapper, UUID>
        implements IProjectRoleService {

    IProjectRolePermissionRepository projectRolePermissionRepository;

    public ProjectRoleService(IProjectRoleRepository projectRoleRepository,
            IProjectRolePermissionRepository projectRolePermissionRepository,
            IProjectRoleMapper projectRoleMapper,
            MessageService messageService) {
        super(projectRoleRepository, projectRoleMapper, messageService, ProjectRole.class);
        this.projectRolePermissionRepository = projectRolePermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectRoleDTO> findAll() {
        return getAllRoles();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectRoleDTO findById(UUID id) {
        return getRoleById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectRoleDTO> getAllRoles() {
        return repository.findByActiveTrueOrderByOrderAsc()
                .stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectRoleDTO getRoleById(UUID id) {
        ProjectRole role = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException("Project role not found"));
        return mapper.entityToDTO(role);
    }

    @Override
    @Transactional
    public ProjectRoleDTO create(ProjectRoleEditorForm form) {
        if (repository.existsByCode(form.getCode())) {
            throw new BWCBusinessException("Role code already exists: " + form.getCode());
        }

        ProjectRole role = mapper.formToEntity(form);
        role.setActive(true);
        role = repository.save(role);

        syncPermissions(role, form.getPermissions());

        return mapper.entityToDTO(repository.findById(role.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public ProjectRoleDTO update(UUID id, ProjectRoleEditorForm form) {
        ProjectRole role = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException("Project role not found"));

        mapper.formToEntity(form, role);
        role = repository.save(role);

        syncPermissions(role, form.getPermissions());

        return mapper.entityToDTO(repository.findById(role.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ProjectRole role = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException("Project role not found"));
        repository.delete(role);
    }

    private void syncPermissions(ProjectRole role, Set<String> permissionValues) {
        projectRolePermissionRepository.deleteByRoleId(role.getId());
        projectRolePermissionRepository.flush();

        if (permissionValues != null && !permissionValues.isEmpty()) {
            List<ProjectRolePermission> permissions = new ArrayList<>();
            for (String pValue : permissionValues) {
                ProjectPermission permission = ProjectPermission.fromValue(pValue);
                permissions.add(ProjectRolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .build());
            }
            projectRolePermissionRepository.saveAll(permissions);
        }
    }
}
