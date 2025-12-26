package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.dtos.project.ProjectDTO;
import com.tnh.baseware.core.entities.project.Project;
import com.tnh.baseware.core.enums.project.ProjectAction;
import com.tnh.baseware.core.enums.project.ProjectStatus;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.project.ProjectEditorForm;
import com.tnh.baseware.core.mappers.project.IProjectMapper;
import com.tnh.baseware.core.repositories.audit.ICategoryRepository;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectService
        extends GenericService<Project, ProjectEditorForm, ProjectDTO, IProjectRepository, IProjectMapper, UUID>
        implements IProjectService {

    public ProjectService(IProjectRepository repository,
            IProjectMapper mapper,
            MessageService messageService) {
        super(repository, mapper, messageService, Project.class);
    }

    @Override
    @Transactional
    public ProjectDTO create(ProjectEditorForm form) {
        Project project = mapper.formToEntity(form);

        project.setStatus(ProjectStatus.DRAFT);
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
}
