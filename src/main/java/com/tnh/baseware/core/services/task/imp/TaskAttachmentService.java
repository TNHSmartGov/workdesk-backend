package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskAttachmentDTO;
import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.entities.task.TaskAttachment;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.forms.task.TaskAttachmentEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskAttachmentMapper;
import com.tnh.baseware.core.repositories.task.ITaskAttachmentRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.doc.IFileDocumentService;
import com.tnh.baseware.core.services.task.ITaskAttachmentService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskAttachmentService extends
        GenericService<TaskAttachment, TaskAttachmentEditorForm, TaskAttachmentDTO, ITaskAttachmentRepository, ITaskAttachmentMapper, UUID>
        implements ITaskAttachmentService {

    IFileDocumentService fileDocumentService;
    ITaskRepository taskRepository;

    public TaskAttachmentService(ITaskAttachmentRepository repository, ITaskAttachmentMapper mapper,
            MessageService messageService, IFileDocumentService fileDocumentService, ITaskRepository taskRepository) {
        super(repository, mapper, messageService, TaskAttachment.class);
        this.fileDocumentService = fileDocumentService;
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskAttachmentDTO uploadFile(MultipartFile fileUpload, UUID taskId, String description) {
        User currentUser = getCurrentUser();
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BWCBusinessException(messageService.getMessage("task.not.found")));
        FileDocument doc = fileDocumentService.upFileDocumentEntity(fileUpload);
        var entity = TaskAttachment.builder()
                .file(doc)
                .task(task)
                .description(description)
                .uploader(currentUser)
                .build();
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    public List<TaskAttachmentDTO> uploadFiles(List<MultipartFile> filesUpload, UUID taskId, String description) {
        User currentUser = getCurrentUser();
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BWCBusinessException(messageService.getMessage("task.not.found")));
        List<TaskAttachment> attachments = new ArrayList<>();
        for (MultipartFile f : filesUpload) {
            FileDocument doc = fileDocumentService.upFileDocumentEntity(f);
            attachments.add(TaskAttachment.builder()
                    .file(doc)
                    .task(task)
                    .description(description)
                    .uploader(currentUser)
                    .build());
        }
        List<TaskAttachment> saved = repository.saveAll(attachments);
        return saved.stream().map(mapper::entityToDTO).collect(Collectors.toList());
    }
}
