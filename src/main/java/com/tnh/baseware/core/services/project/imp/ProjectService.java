package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.entities.user.UserOrganization;
import com.tnh.baseware.core.enums.project.ProjectAction;
import com.tnh.baseware.core.enums.project.ProjectMemberRole;
import com.tnh.baseware.core.enums.project.ProjectPermission;
import com.tnh.baseware.core.enums.project.ProjectStatus;
import com.tnh.baseware.core.enums.project.ProjectType;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.forms.project.ProjectEditorForm;
import com.tnh.baseware.core.mappers.project.IProjectMapper;
import com.tnh.baseware.core.repositories.adu.IOrganizationRepository;
import com.tnh.baseware.core.repositories.project.IProjectMemberRepository;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.repositories.user.IUserOrganizationRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.securities.ProjectSecurityService;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectService;
import com.tnh.baseware.core.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectService
        extends GenericService<Project, ProjectEditorForm, ProjectDTO, IProjectRepository, IProjectMapper, UUID>
        implements IProjectService {
    ITaskListRepository taskListRepository;
    ProjectSecurityService projectSecurityService;
    IProjectMemberRepository projectMemberRepository;
    IUserRepository userRepository;
    IUserOrganizationRepository userOrganizationRepository;
    IOrganizationRepository organizationRepository;
    SecurityUtils securityUtils;

    public ProjectService(IProjectRepository repository,
            IProjectMapper mapper,
            ProjectSecurityService projectSecurityService,
            MessageService messageService,
            IUserRepository userRepository,
            IUserOrganizationRepository userOrganizationRepository,
            IProjectMemberRepository projectMemberRepository,
            ITaskListRepository taskListRepository,
            IOrganizationRepository organizationRepository,
            SecurityUtils securityUtils) {
        super(repository, mapper, messageService, Project.class);
        this.taskListRepository = taskListRepository;
        this.projectSecurityService = projectSecurityService;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.userOrganizationRepository = userOrganizationRepository;
        this.organizationRepository = organizationRepository;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDTO findById(UUID id) {
        UUID orgId = securityUtils.currentOrgId();

        return repository.findByIdAndOrganizationId(id, orgId)
                .map(mapper::entityToDTO)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("project.not.found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> findAll() {
        UUID orgId = securityUtils.currentOrgId();

        return repository.findByOrganizationId(orgId, Sort.by(Sort.Order.desc("createdDate")))
                .stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDTO> findAll(Pageable pageable) {

        Boolean isSystem = isUserSystem();
        if (isSystem) {
            return repository.findAll(pageable)
                    .map(mapper::entityToDTO);
        }
        UUID orgId = securityUtils.currentOrgId();
        return repository.findByOrganizationId(orgId, pageable)
                .map(mapper::entityToDTO);
    }

    @Override
    @Transactional
    public ProjectDTO create(ProjectEditorForm form) {
        UUID orgId = securityUtils.currentOrgId();

        Project project = mapper.formToEntity(form);
        project.setOrganization(
                organizationRepository.findById(orgId).orElseThrow(
                        () -> new BWCNotFoundException(messageService.getMessage("organization.not.found"))));
        project.setStatus(ProjectStatus.DRAFT);
        project = repository.save(project);

        if (form.isCreateDefaultTaskList()) {

            if (StringUtils.isBlank(form.getDefaultTaskListName())) {
                throw new BWCValidationException(
                        "Default task list name must be provided when createDefaultTaskList is enabled");
            }

            taskListRepository.save(
                    TaskList.builder()
                            .project(project)
                            .name(form.getDefaultTaskListName())
                            .isDefault(true)
                            .orderIndex(0)
                            .build());
        }

        var member = ProjectMember.builder()
                .project(project)
                .user(getCurrentUser())
                .role(ProjectMemberRole.OWNER)
                .build();
        projectMemberRepository.save(member);
        return mapper.entityToDTO(repository.save(project));
    }

    @Override
    @Transactional
    public ProjectDTO update(UUID id, ProjectEditorForm form) {
        UUID orgId = securityUtils.currentOrgId();
        var currentUser = getCurrentUser();
        var project = repository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("project.not.found")));
        Set<UserOrganization> userOrgs = userOrganizationRepository.findByUserIdAndActiveTrue(currentUser.getId());

        if (userOrgs.isEmpty() || !projectSecurityService.checkPermission(currentUser, project,
                ProjectPermission.PROJECT_UPDATE, userOrgs)) {
            throw new BWCBusinessException("You do not have permission to update this project");
        }

        mapper.formToEntity(form, project);

        return mapper.entityToDTO(repository.save(project));
    }

    @Override
    @Transactional
    public void performAction(UUID projectId, ProjectAction action) {
        UUID orgId = securityUtils.currentOrgId();
        var project = repository.findByIdAndOrganizationId(projectId, orgId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("project.not.found")));

        switch (action) {
            case PUBLISH -> publish(project);
            case ARCHIVE -> archive(project);
        }
    }

    private void publish(Project project) {
        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new IllegalStateException("Only draft project can be published");
        }
        project.setStatus(ProjectStatus.ACTIVE);
    }

    private void archive(Project project) {
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new IllegalStateException("Only active project can be archived");
        }
        project.setStatus(ProjectStatus.ARCHIVED);
    }

    @Deprecated(forRemoval = false)
    public Project getOrCreatePersonalProject(UUID userId) {
        return repository.findPersonalByUser(userId)
                .orElseGet(() -> {
                    String projectCode = "PERS_" + userId;
                    Project project;
                    try {
                        project = repository.save(
                                Project.builder()
                                        .name("Personal Project")
                                        .code(projectCode)
                                        .type(ProjectType.PERSONAL)
                                        .status(ProjectStatus.ACTIVE)
                                        .build());
                    } catch (DataIntegrityViolationException e) {
                        project = repository.findByCode(projectCode)
                                .orElseThrow(() -> new BWCNotFoundException("Project not found after duplicate error"));
                    }

                    try {
                        projectMemberRepository.save(
                                ProjectMember.builder()
                                        .project(project)
                                        .user(userRepository.getReferenceById(userId))
                                        .role(ProjectMemberRole.OWNER)
                                        .build());
                    } catch (DataIntegrityViolationException ignore) {
                    }

                    if (!taskListRepository.existsByProjectId(project.getId())) {
                        taskListRepository.save(
                                TaskList.builder()
                                        .project(project)
                                        .name("My Tasks")
                                        .isDefault(true)
                                        .orderIndex(0)
                                        .build());
                    }

                    return project;
                });
    }
}
