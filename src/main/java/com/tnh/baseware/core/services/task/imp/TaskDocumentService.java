package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskDocumentDTO;
import com.tnh.baseware.core.entities.task.TaskDocument;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.task.TaskDocumentEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskDocumentMapper;
import com.tnh.baseware.core.repositories.doc.IDocumentRepository;
import com.tnh.baseware.core.repositories.task.ITaskDocumentRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskDocumentService;
import com.tnh.baseware.core.components.GenericEntityFetcher;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskDocumentService extends
        GenericService<TaskDocument, TaskDocumentEditorForm, TaskDocumentDTO, ITaskDocumentRepository, ITaskDocumentMapper, UUID>
        implements ITaskDocumentService {

    ITaskRepository taskRepository;
    IDocumentRepository documentRepository;
    GenericEntityFetcher fetcher;

    public TaskDocumentService(ITaskDocumentRepository repository,
            ITaskDocumentMapper mapper,
            MessageService messageService,
            ITaskRepository taskRepository,
            IDocumentRepository documentRepository,
            GenericEntityFetcher fetcher) {
        super(repository, mapper, messageService, TaskDocument.class);
        this.taskRepository = taskRepository;
        this.documentRepository = documentRepository;
        this.fetcher = fetcher;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TaskDocumentDTO create(TaskDocumentEditorForm form) {
        var entity = mapper.formToEntity(form, fetcher, taskRepository, documentRepository);
        var saved = repository.save(entity);
        return mapper.entityToDTO(saved);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TaskDocumentDTO update(UUID id, TaskDocumentEditorForm form) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("task.document.not.found", id)));
        mapper.updateFromForm(form, entity, fetcher, taskRepository, documentRepository);
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDocumentDTO> getDocumentsByTask(UUID taskId) {
        // Verify task exists
        taskRepository.findById(taskId)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("task.not.found", taskId)));

        return repository.findAllByTask_Id(taskId).stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDocumentDTO> getTasksByDocument(UUID documentId) {
        // Verify document exists
        documentRepository.findById(documentId).orElseThrow(
                () -> new BWCNotFoundException(messageService.getMessage("document.not.found", documentId)));

        return repository.findAllByDocument_Id(documentId).stream()
                .map(mapper::entityToDTO)
                .toList();
    }
}
