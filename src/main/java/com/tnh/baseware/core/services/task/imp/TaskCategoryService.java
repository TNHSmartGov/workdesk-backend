package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.task.TaskCategoryDTO;
import com.tnh.baseware.core.entities.task.TaskCategory;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.task.TaskCategoryEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskCategoryMapper;
import com.tnh.baseware.core.repositories.task.ITaskCategoryRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskCategoryService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskCategoryService extends
        GenericService<TaskCategory, TaskCategoryEditorForm, TaskCategoryDTO, ITaskCategoryRepository, ITaskCategoryMapper, UUID>
        implements ITaskCategoryService {

    GenericEntityFetcher fetcher;

    public TaskCategoryService(ITaskCategoryRepository repository,
            ITaskCategoryMapper mapper,
            MessageService messageService,
            GenericEntityFetcher fetcher) {
        super(repository, mapper, messageService, TaskCategory.class);
        this.fetcher = fetcher;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TaskCategoryDTO create(TaskCategoryEditorForm form) {
        var entity = mapper.formToEntity(form, fetcher, repository);
        var saved = repository.save(entity);
        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TaskCategoryDTO update(UUID id, TaskCategoryEditorForm form) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("task.category.not.found", id)));
        mapper.updateFromForm(form, entity, fetcher, repository);
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCategoryDTO> findAll() {
        var categories = repository.findAllWithParent();
        return mapper.mapTaskCategoriesToTree(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskCategoryDTO> findAll(Pageable pageable) {
        var sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.asc("orderIndex")));

        var allCategories = repository.findAllWithParent();
        var parentCategories = repository.findAllParent("parent", sortedPageable);

        var parentMap = allCategories.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        var tree = parentCategories.getContent().stream()
                .sorted((c1, c2) -> Integer.compare(c1.getOrderIndex(), c2.getOrderIndex()))
                .map(c -> mapper.buildTaskCategoryTree(c, parentMap))
                .toList();

        return new PageImpl<>(tree, sortedPageable, parentCategories.getTotalElements());
    }
}
