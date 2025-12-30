package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.entities.task.TaskList;
import com.tnh.baseware.core.enums.project.ProjectAction;
import com.tnh.baseware.core.enums.project.ProjectMemberRole;
import com.tnh.baseware.core.enums.project.ProjectPermission;
import com.tnh.baseware.core.enums.project.ProjectStatus;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.forms.project.ProjectEditorForm;
import com.tnh.baseware.core.mappers.project.IProjectMapper;
import com.tnh.baseware.core.repositories.project.IProjectMemberRepository;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.securities.ProjectSecurityService;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectMemberService;
import com.tnh.baseware.core.services.project.IProjectService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectService
        extends GenericService<Project, ProjectEditorForm, ProjectDTO, IProjectRepository, IProjectMapper, UUID>
        implements IProjectService {
    ITaskListRepository taskListRepository;
    ProjectSecurityService projectSecurityService;
    IProjectMemberRepository projectMemberRepository;

    public ProjectService(IProjectRepository repository,
            IProjectMapper mapper,
            ProjectSecurityService projectSecurityService,
            MessageService messageService,
            IProjectMemberRepository projectMemberRepository,
            ITaskListRepository taskListRepository) {
        super(repository, mapper, messageService, Project.class);
        this.taskListRepository = taskListRepository;
        this.projectSecurityService = projectSecurityService;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    @Transactional
    public ProjectDTO create(ProjectEditorForm form) {
        Project project = mapper.formToEntity(form);
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
        var currentUser = getCurrentUser();
        if (!projectSecurityService.checkPermission(currentUser.getId(), id, ProjectPermission.PROJECT_UPDATE)) {
            throw new BWCNotFoundException("Project not found");
        }
        var project = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException("Project not found"));
        mapper.formToEntity(form, project);

        return mapper.entityToDTO(repository.save(project));
    }

    @Override
    @Transactional
    public void performAction(UUID projectId, ProjectAction action) {

        Project project = repository.findById(projectId)
                .orElseThrow(() -> new BWCNotFoundException("Project not found"));

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
    public List<Project> getProjectByOrganizationId(UUID organizationId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProjectByOrganizationId'");
    }

    @Override
    public Page<Project> getProjectByOrganizationId(UUID organizationId, int page, int size) {
        // TODO Auto-generated method stub
        return null;
    }
}
