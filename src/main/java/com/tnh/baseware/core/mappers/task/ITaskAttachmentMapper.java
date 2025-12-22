package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskAttachment;
import com.tnh.baseware.core.forms.task.TaskAttachmentEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.mappers.doc.IFileDocumentMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {IFileDocumentMapper.class})
public interface ITaskAttachmentMapper extends IGenericMapper<TaskAttachment, TaskAttachmentEditorForm, TaskAttachmentDTO> {

    @Override
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "uploaderId", source = "uploader.id")
    @Mapping(target = "file", source = "file")
    TaskAttachmentDTO entityToDTO(TaskAttachment entity);

    @Override
    @Mapping(target = "task.id", source = "taskId")
    @Mapping(target = "file.id", source = "fileId")
    TaskAttachment formToEntity(TaskAttachmentEditorForm form);
}
