package com.tnh.baseware.core.services.adu.imp;

import com.tnh.baseware.core.dtos.adu.OrganizationDTO;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.entities.audit.Category;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.entities.user.UserOrganization;
import com.tnh.baseware.core.enums.CategoryCode;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.adu.OrganizationEditorForm;
import com.tnh.baseware.core.forms.user.AssignUserEditorForm;
import com.tnh.baseware.core.mappers.adu.IOrganizationMapper;
import com.tnh.baseware.core.repositories.adu.IOrganizationRepository;
import com.tnh.baseware.core.repositories.audit.ICategoryRepository;
import com.tnh.baseware.core.repositories.user.IUserOrganizationRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.adu.IOrganizationService;
import com.tnh.baseware.core.utils.BasewareUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrganizationService extends
                GenericService<Organization, OrganizationEditorForm, OrganizationDTO, IOrganizationRepository, IOrganizationMapper, UUID>
                implements
                IOrganizationService {

        IUserRepository userRepository;
        IUserOrganizationRepository userOrganizationRepository;
        ICategoryRepository categoryRepository;

        public OrganizationService(IOrganizationRepository repository,
                        IOrganizationMapper mapper,
                        MessageService messageService,
                        IUserRepository userRepository,
                        IUserOrganizationRepository userOrganizationRepository,
                        ICategoryRepository categoryRepository) {
                super(repository, mapper, messageService, Organization.class);
                this.userRepository = userRepository;
                this.userOrganizationRepository = userOrganizationRepository;
                this.categoryRepository = categoryRepository;
        }

        @Override
        @Transactional(readOnly = true)
        public List<OrganizationDTO> findAll() {
                var organizations = repository.findAll();
                return mapper.mapOrganizationsToTree(organizations);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<OrganizationDTO> findAll(Pageable pageable) {
                var sortedPageable = PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                Sort.by(Sort.Order.desc("createdDate")));

                var allOrganizations = repository.findAllWithParent();
                var parentOrganizations = repository.findAllParent("parent", sortedPageable);

                var parentMap = allOrganizations.stream()
                                .filter(o -> o.getParent() != null)
                                .collect(Collectors.groupingBy(o -> o.getParent().getId()));

                var tree = parentOrganizations.getContent().stream()
                                .map(o -> mapper.buildOrganizationTree(o, parentMap))
                                .toList();

                return new PageImpl<>(tree, sortedPageable, parentOrganizations.getTotalElements());
        }

        @Override
        @Transactional(isolation = Isolation.READ_COMMITTED)
        public void assignOrganizations(UUID id, List<UUID> ids) {
                if (BasewareUtils.isBlank(ids)) {
                        return;
                }

                var parent = repository.findById(id).orElseThrow(() -> new BWCNotFoundException(
                                messageService.getMessage("organization.not.found", id)));

                var children = repository.findAllById(ids);
                if (BasewareUtils.isBlank(children)) {
                        return;
                }

                children.stream()
                                .filter(child -> !parent.equals(child.getParent()))
                                .forEach(child -> child.setParent(parent));

                repository.saveAll(children);
        }

        @Override
        @Transactional(isolation = Isolation.READ_COMMITTED)
        public void removeOrganizations(UUID id, List<UUID> ids) {
                if (BasewareUtils.isBlank(ids)) {
                        return;
                }

                var parent = repository.findById(id).orElseThrow(() -> new BWCNotFoundException(
                                messageService.getMessage("organization.not.found", id)));

                var children = repository.findAllById(ids);
                if (BasewareUtils.isBlank(children)) {
                        return;
                }

                children.stream()
                                .filter(child -> parent.equals(child.getParent()))
                                .forEach(child -> child.setParent(null));

                repository.saveAll(children);
        }

        @Override
        @Transactional(isolation = Isolation.READ_COMMITTED)
        public void assignUsers(UUID id, List<AssignUserEditorForm> forms) {
                if (BasewareUtils.isBlank(forms)) {
                        return;
                }

                var organization = repository.findById(id)
                                .orElseThrow(() -> new BWCNotFoundException(
                                                messageService.getMessage("organization.not.found", id)));

                // Nếu userId bị trùng -> lấy bản mới nhất
                Map<UUID, AssignUserEditorForm> distinctFormsMap = forms.stream()
                                .collect(Collectors.toMap(
                                                AssignUserEditorForm::getUserId,
                                                Function.identity(),
                                                (existing, replacement) -> replacement));

                Collection<AssignUserEditorForm> distinctForms = distinctFormsMap.values();

                Set<UUID> userIds = distinctFormsMap.keySet();
                Set<UUID> titleIds = distinctForms.stream()
                                .map(AssignUserEditorForm::getTitleId)
                                .collect(Collectors.toSet());

                Map<UUID, User> userMap = userRepository.findAllById(userIds)
                                .stream()
                                .collect(Collectors.toMap(User::getId, Function.identity()));

                if (userMap.size() != userIds.size()) {
                        throw new BWCNotFoundException(messageService.getMessage("user.not.found"));
                }

                Map<UUID, Category> titleMap = categoryRepository.findByCodeAndIdIn(
                                CategoryCode.ORGANIZATION_TITLE, titleIds).stream()
                                .collect(Collectors.toMap(Category::getId, Function.identity()));

                if (titleMap.size() != titleIds.size()) {
                        throw new BWCNotFoundException(messageService.getMessage("title.not.found"));
                }

                List<UserOrganization> existingAssignments = userOrganizationRepository
                                .findByOrganizationIdAndUserIdInAndActiveTrue(id, userIds);

                Map<UUID, UserOrganization> existingMap = existingAssignments.stream()
                                .collect(Collectors.toMap(uo -> uo.getUser().getId(), Function.identity()));

                List<UserOrganization> toSave = new ArrayList<>();

                for (AssignUserEditorForm form : distinctForms) {
                        UserOrganization uo = existingMap.get(form.getUserId());

                        if (uo == null) {
                                uo = UserOrganization.builder()
                                                .user(userMap.get(form.getUserId()))
                                                .organization(organization)
                                                .title(titleMap.get(form.getTitleId()))
                                                .active(true)
                                                .build();
                                toSave.add(uo);
                        } else {
                                if (!uo.getTitle().getId().equals(form.getTitleId())) {
                                        uo.setTitle(titleMap.get(form.getTitleId()));
                                        uo.setActive(true);
                                        toSave.add(uo);
                                }
                        }
                }

                if (!toSave.isEmpty()) {
                        userOrganizationRepository.saveAll(toSave);
                }
        }

        @Override
        @Transactional(isolation = Isolation.READ_COMMITTED)
        public void removeUsers(UUID organizationId, List<UUID> userIds) {

                if (BasewareUtils.isBlank(userIds)) {
                        return;
                }

                boolean orgExists = repository.existsById(organizationId);
                if (!orgExists) {
                        throw new BWCNotFoundException(
                                        messageService.getMessage("organization.not.found", organizationId));
                }

                int updated = userOrganizationRepository.deactivateUsers(
                                organizationId,
                                userIds);

                if (updated == 0) {
                        throw new BWCNotFoundException(
                                        messageService.getMessage("users.not.in.organization"));
                }
        }

        @Override
        @Transactional(isolation = Isolation.READ_COMMITTED)
        public void changeTitle(UUID orgId, UUID userId, UUID titleId) {

                UserOrganization uo = userOrganizationRepository
                                .findByUserIdAndOrganizationIdAndActiveTrue(userId, orgId)
                                .orElseThrow(() -> new BWCNotFoundException(
                                                messageService.getMessage("user.not.in.organization", userId, orgId)));

                if (uo.getTitle().getId().equals(titleId)) {
                        return;
                }

                Category titleCategory = categoryRepository
                                .findByCodeAndId(CategoryCode.ORGANIZATION_TITLE, titleId)
                                .orElseThrow(() -> new BWCNotFoundException(
                                                messageService.getMessage("title.not.found", titleId)));

                uo.setTitle(titleCategory);
        }

        @Override
        public List<OrganizationDTO> findAllOfUser(UUID user) {
                userRepository.findById(user).orElseThrow(
                                () -> new BWCNotFoundException(
                                                messageService.getMessage("user.not.found")));
                Set<UserOrganization> userOrganizations = userOrganizationRepository
                                .findByUserIdAndActiveTrue(user);
                // get organization array
                return userOrganizations.stream()
                                .map(UserOrganization::getOrganization).map(mapper::entityToDTO).toList();

        }
}
