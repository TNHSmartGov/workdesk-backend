package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectAttachment;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.entities.task.Task;
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
import com.tnh.baseware.core.repositories.project.IProjectAttachmentRepository;
import com.tnh.baseware.core.repositories.project.IProjectMemberRepository;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskAttachmentRepository;
import com.tnh.baseware.core.repositories.task.ITaskCommentAttachmentRepository;
import com.tnh.baseware.core.repositories.task.ITaskCommentRepository;
import com.tnh.baseware.core.repositories.task.ITaskDependencyRepository;
import com.tnh.baseware.core.repositories.task.ITaskDocumentRepository;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.repositories.task.ITaskRequirementRepository;
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
    IProjectAttachmentRepository projectAttachmentRepository;
    ITaskRepository taskRepository;
    ITaskMemberRepository taskMemberRepository;
    ITaskCommentRepository taskCommentRepository;
    ITaskAttachmentRepository taskAttachmentRepository;
    ITaskActivityLogRepository taskActivityLogRepository;
    ITaskCommentAttachmentRepository taskCommentAttachmentRepository;
    ITaskDocumentRepository taskDocumentRepository;
    ITaskRequirementRepository taskRequirementRepository;
    ITaskDependencyRepository taskDependencyRepository;

    public ProjectService(IProjectRepository repository,
            IProjectMapper mapper,
            ProjectSecurityService projectSecurityService,
            MessageService messageService,
            IUserRepository userRepository,
            IUserOrganizationRepository userOrganizationRepository,
            IProjectMemberRepository projectMemberRepository,
            ITaskListRepository taskListRepository,
            IOrganizationRepository organizationRepository,
            SecurityUtils securityUtils,
            IProjectAttachmentRepository projectAttachmentRepository,
            ITaskRepository taskRepository,
            ITaskMemberRepository taskMemberRepository,
            ITaskCommentRepository taskCommentRepository,
            ITaskAttachmentRepository taskAttachmentRepository,
            ITaskActivityLogRepository taskActivityLogRepository,
            ITaskCommentAttachmentRepository taskCommentAttachmentRepository,
            ITaskDocumentRepository taskDocumentRepository,
            ITaskRequirementRepository taskRequirementRepository,
            ITaskDependencyRepository taskDependencyRepository) {
        super(repository, mapper, messageService, Project.class);
        this.taskListRepository = taskListRepository;
        this.projectSecurityService = projectSecurityService;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.userOrganizationRepository = userOrganizationRepository;
        this.organizationRepository = organizationRepository;
        this.securityUtils = securityUtils;
        this.projectAttachmentRepository = projectAttachmentRepository;
        this.taskRepository = taskRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.taskActivityLogRepository = taskActivityLogRepository;
        this.taskCommentAttachmentRepository = taskCommentAttachmentRepository;
        this.taskDocumentRepository = taskDocumentRepository;
        this.taskRequirementRepository = taskRequirementRepository;
        this.taskDependencyRepository = taskDependencyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDTO findById(UUID id) {
        Boolean isSystem = isUserSystem();
        if (isSystem) {
            return repository.findById(id)
                    .map(mapper::entityToDTO)
                    .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("project.not.found")));
        }
        UUID orgId = securityUtils.currentOrgId();

        return repository.findByIdAndOrganizationId(id, orgId)
                .map(mapper::entityToDTO)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("project.not.found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> findAll() {
        Boolean isSystem = isUserSystem();
        if (isSystem) {
            return repository.findAll()
                    .stream()
                    .map(mapper::entityToDTO)
                    .toList();
        }

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

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID orgId = securityUtils.currentOrgId();
        var currentUser = getCurrentUser();

        // Find and verify project exists and belongs to user's organization
        var project = repository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("project.not.found")));

        // Check permission
        Set<UserOrganization> userOrgs = userOrganizationRepository.findByUserIdAndActiveTrue(currentUser.getId());
        if (userOrgs.isEmpty() || !projectSecurityService.checkPermission(currentUser, project,
                ProjectPermission.PROJECT_DELETE, userOrgs)) {
            throw new BWCBusinessException("You do not have permission to delete this project");
        }

        // Deep delete: Delete all related data in proper order

        // 1. Get all task lists for this project
        List<TaskList> taskLists = taskListRepository.findByProjectIdOrderByOrderIndexAsc(id);

        // 2. For each task list, delete all tasks and their related data
        for (TaskList taskList : taskLists) {
            // Get all tasks in this task list
            List<Task> tasks = taskList.getTasks();
            if (tasks != null && !tasks.isEmpty()) {
                for (Task task : tasks) {
                    UUID taskId = task.getId();

                    // Delete task-related entities
                    // Note: For entities without custom findBy methods, we use filter on loaded
                    // data

                    // Delete task comment attachments
                    taskCommentAttachmentRepository.deleteAll(
                            taskCommentAttachmentRepository.findAll().stream()
                                    .filter(tca -> tca.getComment() != null
                                            && taskId.equals(tca.getComment().getTask().getId()))
                                    .toList());

                    // Delete task comments
                    taskCommentRepository.deleteAll(
                            taskCommentRepository.findAll().stream()
                                    .filter(tc -> taskId.equals(tc.getTask().getId()))
                                    .toList());

                    // Delete task attachments
                    taskAttachmentRepository.deleteAll(
                            taskAttachmentRepository.findAll().stream()
                                    .filter(ta -> taskId.equals(ta.getTask().getId()))
                                    .toList());

                    // Delete task members
                    taskMemberRepository.deleteAll(taskMemberRepository.findByTaskId(taskId));

                    // Delete task activity logs
                    taskActivityLogRepository.deleteAll(
                            taskActivityLogRepository.findAll().stream()
                                    .filter(tal -> taskId.equals(tal.getTask().getId()))
                                    .toList());

                    // Delete task documents
                    taskDocumentRepository.deleteAll(taskDocumentRepository.findAllByTask_Id(taskId));

                    // Delete task dependencies (both as fromTask and toTask)
                    taskDependencyRepository.deleteAll(
                            taskDependencyRepository.findAll().stream()
                                    .filter(tdep -> taskId.equals(tdep.getFromTask().getId())
                                            || taskId.equals(tdep.getToTask().getId()))
                                    .toList());

                    // Delete task requirements
                    taskRequirementRepository.deleteAll(taskRequirementRepository.findByTaskId(taskId));
                }

                // Delete all tasks in this task list
                taskRepository.deleteAll(tasks);
            }
        }

        // 3. Delete all task lists
        taskListRepository.deleteAll(taskLists);

        // 4. Delete project attachments
        List<ProjectAttachment> projectAttachments = projectAttachmentRepository.findByProject_Id(id);
        projectAttachmentRepository.deleteAll(projectAttachments);

        // 5. Delete project members
        List<ProjectMember> projectMembers = projectMemberRepository.findDistinctByProject_Id(id);
        projectMemberRepository.deleteAll(projectMembers);

        // 6. Finally, delete the project itself
        repository.delete(project);
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
