package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.dtos.project.SprintDTO;
import com.tnh.baseware.core.entities.project.Sprint;
import com.tnh.baseware.core.enums.project.SprintStatus;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.project.SprintEditorForm;
import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.mappers.project.ISprintMapper;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.project.ISprintRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.ISprintService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SprintService
        extends GenericService<Sprint, SprintEditorForm, SprintDTO, ISprintRepository, ISprintMapper, UUID>
        implements ISprintService {

    IProjectRepository projectRepository;
    GenericEntityFetcher fetcher;

    public SprintService(ISprintRepository repository, ISprintMapper mapper, MessageService messageService,
            IProjectRepository projectRepository, GenericEntityFetcher fetcher) {
        super(repository, mapper, messageService, Sprint.class);
        this.projectRepository = projectRepository;
        this.fetcher = fetcher;
    }

    @Override
    @Transactional
    public SprintDTO create(SprintEditorForm form) {
        Sprint entity = mapper.formToEntity(form, fetcher, projectRepository);
        repository.save(entity);
        return mapper.entityToDTO(entity);
    }

    @Override
    @Transactional
    public SprintDTO update(UUID id, SprintEditorForm form) {
        Sprint entity = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("sprint.not.found", id)));
        mapper.updateEntityFromForm(form, entity, fetcher, projectRepository);
        repository.save(entity);
        return mapper.entityToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintDTO> findAllByProjectId(UUID projectId) {
        return repository.findAllByProjectIdWithStats(projectId).stream()
                .map(proj -> {
                    Sprint sprint = repository.findById(proj.getId()).orElse(null); // Optimized query usually returns
                                                                                    // entities, but here we used
                                                                                    // projection.
                    // Re-mapping pattern: Fetch entities or map from projection.
                    // Better approach with existing mapper: just map properties
                    SprintDTO dto = mapper.entityToDTO(sprint);
                    dto.setTotalStoryPoints(
                            proj.getTotalStoryPoints() != null ? proj.getTotalStoryPoints().intValue() : 0);
                    dto.setCompletedStoryPoints(
                            proj.getCompletedStoryPoints() != null ? proj.getCompletedStoryPoints().intValue() : 0);
                    if (dto.getTotalStoryPoints() > 0) {
                        dto.setProgress((dto.getCompletedStoryPoints() * 100) / dto.getTotalStoryPoints());
                    } else {
                        dto.setProgress(0);
                    }
                    return dto;
                }).toList();
    }

    @Override
    @Transactional
    public void startSprint(UUID id) {
        Sprint sprint = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("sprint.not.found", id)));
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setStartDate(Instant.now());
        repository.save(sprint);
    }

    @Override
    @Transactional
    public void completeSprint(UUID id) {
        Sprint sprint = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("sprint.not.found", id)));
        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setCompletedDate(Instant.now());
        repository.save(sprint);
    }
}
