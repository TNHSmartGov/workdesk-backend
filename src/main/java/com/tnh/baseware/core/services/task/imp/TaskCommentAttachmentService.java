package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskCommentAttachmentDTO;
import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.entities.task.TaskCommentAttachment;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.forms.task.TaskCommentAttachmentEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskCommentAttachmentMapper;
import com.tnh.baseware.core.repositories.task.ITaskCommentAttachmentRepository;
import com.tnh.baseware.core.repositories.task.ITaskCommentRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.doc.IFileDocumentService;
import com.tnh.baseware.core.services.task.ITaskCommentAttachmentService;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskCommentAttachmentService extends
        GenericService<TaskCommentAttachment, TaskCommentAttachmentEditorForm, TaskCommentAttachmentDTO, ITaskCommentAttachmentRepository, ITaskCommentAttachmentMapper, UUID>
        implements ITaskCommentAttachmentService {

    IFileDocumentService fileDocumentService;
    ITaskCommentRepository taskCommentRepository;

    public TaskCommentAttachmentService(ITaskCommentAttachmentRepository repository,
            ITaskCommentAttachmentMapper mapper, MessageService messageService,
            ITaskCommentRepository taskCommentRepository,
            IFileDocumentService fileDocumentService) {
        super(repository, mapper, messageService, TaskCommentAttachment.class);
        this.fileDocumentService = fileDocumentService;
        this.taskCommentRepository = taskCommentRepository;
    }

    @Override
    @Transactional
    public TaskCommentAttachmentDTO uploadAttachment(UUID commentId, MultipartFile file) {
        FileDocument doc = fileDocumentService.upFileDocumentEntity(file);
        var comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new BWCBusinessException(messageService.getMessage("task.comment.not.found")));
        var entity = TaskCommentAttachment.builder()
                .comment(comment)
                .file(doc)
                .uploader(getCurrentUser())
                .build();
        return mapper.entityToDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public List<TaskCommentAttachmentDTO> uploadAttachments(UUID commentId, List<MultipartFile> files) {
        var comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new BWCBusinessException(messageService.getMessage("task.comment.not.found")));
        var entities = files.stream()
                .map(file -> {
                    FileDocument doc = fileDocumentService.upFileDocumentEntity(file);
                    return TaskCommentAttachment.builder()
                            .comment(comment)
                            .file(doc)
                            .uploader(getCurrentUser())
                            .build();
                })
                .collect(Collectors.toList());
        repository.saveAll(entities);
        return entities.stream().map(mapper::entityToDTO).collect(Collectors.toList());
    }

    @Override
    public List<TaskCommentAttachmentDTO> findByCommentId(UUID commentId) {
        taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new BWCBusinessException(messageService.getMessage("task.comment.not.found")));

        return repository.findByComment_Id(commentId).stream().map(mapper::entityToDTO)
                .collect(Collectors.toList());
    }
}
