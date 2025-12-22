package com.tnh.baseware.core.services.user.imp;

import com.tnh.baseware.core.dtos.user.PrivilegeDTO;
import com.tnh.baseware.core.entities.user.Privilege;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.user.PrivilegeEditorForm;
import com.tnh.baseware.core.mappers.user.IPrivilegeMapper;
import com.tnh.baseware.core.repositories.user.IPrivilegeRepository;
import com.tnh.baseware.core.repositories.user.IRoleRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.user.IPrivilegesService;
import com.tnh.baseware.core.utils.PrivilegeExtractorUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PrivilegeService extends GenericService<Privilege, PrivilegeEditorForm, PrivilegeDTO, IPrivilegeRepository, IPrivilegeMapper, UUID> implements IPrivilegesService {

    PrivilegeCacheService privilegeCacheService;
    IRoleRepository roleRepository;
    IPrivilegeMapper privilegeMapper;

    public PrivilegeService(IPrivilegeRepository repository,
                            IPrivilegeMapper mapper,
                            MessageService messageService,
                            PrivilegeCacheService privilegeCacheService,
                            IRoleRepository roleRepository,
                            IPrivilegeMapper privilegeMapper) {
        super(repository, mapper, messageService, Privilege.class);
        this.privilegeCacheService = privilegeCacheService;
        this.roleRepository = roleRepository;
        this.privilegeMapper = privilegeMapper;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PrivilegeDTO update(UUID id, PrivilegeEditorForm form) {
        var privilege = repository.findById(id).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("privilege.not.found", id)));
        mapper.formToEntity(form, privilege);

        var privilegeNew = repository.save(privilege);
        privilegeCacheService.invalidatePrivilegeCache(privilege);

        return mapper.entityToDTO(privilegeNew);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(UUID id) {
        var privilege = repository.findById(id).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("privilege.not.found", id)));
        repository.delete(privilege);

        privilegeCacheService.invalidatePrivilegeCache(privilege);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void syncPrivileges() {
        var extracted = PrivilegeExtractorUtils.extractAllPrivileges("com.tnh.baseware.core.resources");

        var existingPatterns = repository.findAll().stream()
                .map(Privilege::getApiPattern)
                .collect(Collectors.toSet());

        var newPrivileges = extracted.stream()
                .filter(p -> !existingPatterns.contains(p.apiPattern()))
                .map(p -> Privilege.builder()
                        .name(p.name())
                        .resourceName(p.resourceName())
                        .description(p.description())
                        .apiPattern(p.apiPattern())
                        .build())
                .toList();

        repository.saveAll(newPrivileges);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllResourceNames() {
        return repository.findAllResourceNames();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrivilegeDTO> findAllByResourceName(String resourceName) {
        return repository.findAllByField("resourceName", resourceName).stream()
                .map(privilegeMapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrivilegeDTO> findAllByResourceName(String resourceName, Pageable pageable) {
        return repository.findAllByField("resourceName", resourceName, pageable).map(privilegeMapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrivilegeDTO> findAllByRole(UUID roleId) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("role.not.found", roleId)));
        return repository.findAllByEntitiesContaining("roles", role).stream()
                .map(privilegeMapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrivilegeDTO> findAllByRole(UUID roleId, Pageable pageable) {
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("role.not.found", roleId)));
        return repository.findAllByEntitiesContaining("roles", role, pageable).map(privilegeMapper::entityToDTO);
    }
}
