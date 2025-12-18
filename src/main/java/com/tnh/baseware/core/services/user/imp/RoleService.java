package com.tnh.baseware.core.services.user.imp;

import com.tnh.baseware.core.dtos.user.RoleDTO;
import com.tnh.baseware.core.entities.user.Role;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.user.RoleEditorForm;
import com.tnh.baseware.core.mappers.user.IRoleMapper;
import com.tnh.baseware.core.repositories.user.IMenuRepository;
import com.tnh.baseware.core.repositories.user.IPrivilegeRepository;
import com.tnh.baseware.core.repositories.user.IRoleRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.user.IRoleService;
import com.tnh.baseware.core.utils.BasewareUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RoleService extends GenericService<Role, RoleEditorForm, RoleDTO, IRoleRepository, IRoleMapper, UUID> implements IRoleService {

    IMenuRepository menuRepository;
    IUserRepository userRepository;
    IPrivilegeRepository privilegeRepository;
    PrivilegeCacheService privilegeCacheService;

    public RoleService(IRoleRepository repository,
                       IRoleMapper mapper,
                       MessageService messageService,
                       IMenuRepository menuRepository,
                       IUserRepository userRepository,
                       IPrivilegeRepository privilegeRepository,
                       PrivilegeCacheService privilegeCacheService) {
        super(repository, mapper, messageService, Role.class);
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.privilegeRepository = privilegeRepository;
        this.privilegeCacheService = privilegeCacheService;
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(UUID id) {
        var role = repository.findById(id).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("role.not.found", id)));
        repository.delete(role);
        privilegeCacheService.invalidateAllUserPrivilegesByRole(role);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void assignUsers(UUID roleId, List<UUID> ids) {
        if (BasewareUtils.isBlank(ids)) return;

        var role = repository.findById(roleId).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("role.not.found", roleId)));
        var users = userRepository.findAllById(ids);

        if (BasewareUtils.isBlank(users)) return;
        users.forEach(user -> user.getRoles().add(role));

        userRepository.saveAll(users);
        privilegeCacheService.invalidateAllUserPrivilegesByRole(role);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeUsers(UUID roleId, List<UUID> ids) {
        if (BasewareUtils.isBlank(ids)) return;

        var role = repository.findById(roleId).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("role.not.found", roleId)));
        var users = userRepository.findAllById(ids);

        if (BasewareUtils.isBlank(users)) return;
        users.forEach(user -> user.getRoles().remove(role));

        userRepository.saveAll(users);
        privilegeCacheService.invalidateAllUserPrivilegesByRole(role);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void assignPrivileges(UUID roleId, List<UUID> ids) {
        if (BasewareUtils.isBlank(ids)) return;

        var role = repository.findById(roleId).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("role.not.found", roleId)));
        var privileges = privilegeRepository.findAllById(ids);

        if (BasewareUtils.isBlank(privileges)) return;
        role.getPrivileges().addAll(privileges);

        repository.save(role);
        privilegeCacheService.invalidateAllUserPrivilegesByRole(role);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removePrivileges(UUID roleId, List<UUID> ids) {
        if (BasewareUtils.isBlank(ids)) return;

        var role = repository.findById(roleId).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("role.not.found", roleId)));
        var privileges = privilegeRepository.findAllById(ids);

        if (BasewareUtils.isBlank(privileges)) return;
        privileges.forEach(role.getPrivileges()::remove);

        repository.save(role);
        privilegeCacheService.invalidateAllUserPrivilegesByRole(role);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void assignMenus(UUID id, List<UUID> ids) {
        if (BasewareUtils.isBlank(ids)) return;

        var role = repository.findById(id).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("role.not.found", id)));
        var menus = menuRepository.findAllById(ids);

        if (BasewareUtils.isBlank(menus)) return;
        menus.forEach(menu -> menu.getRoles().add(role));

        menuRepository.saveAll(menus);
        privilegeCacheService.invalidateAllUserPrivilegesByRole(role);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeMenus(UUID id, List<UUID> ids) {
        if (BasewareUtils.isBlank(ids)) return;

        var role = repository.findById(id).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("role.not.found", id)));
        var menus = menuRepository.findAllById(ids);

        if (BasewareUtils.isBlank(menus)) return;
        menus.forEach(menu -> menu.getRoles().remove(role));

        menuRepository.saveAll(menus);
        privilegeCacheService.invalidateAllUserPrivilegesByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> findAllByMenu(UUID menuId) {
        var menu = menuRepository.findById(menuId).orElseThrow(() ->
                new BWCNotFoundException(messageService.getMessage("menu.not.found", menuId)));
        return repository.findAllByEntitiesContaining("menus", menu)
                .stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> findAllByPrivilege(UUID privilegeId) {
        var privilege = privilegeRepository.findById(privilegeId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("privilege.not.found", privilegeId)));
        return repository.findAllByEntitiesContaining("privileges", privilege)
                .stream()
                .map(mapper::entityToDTO)
                .toList();
    }
}
