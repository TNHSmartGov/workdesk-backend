package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskCommentAttachmentDTO;
import com.tnh.baseware.core.entities.task.TaskCommentAttachment;
import com.tnh.baseware.core.forms.task.TaskCommentAttachmentEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.mappers.doc.IFileDocumentMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {IFileDocumentMapper.class})
public interface ITaskCommentAttachmentMapper extends IGenericMapper<TaskCommentAttachment, TaskCommentAttachmentEditorForm, TaskCommentAttachmentDTO> {

    @Override
    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "file", source = "file")
    @Mapping(target = "uploaderId", source = "uploader.id")
    TaskCommentAttachmentDTO entityToDTO(TaskCommentAttachment entity);

    @Override
    @Mapping(target = "comment.id", source = "commentId")
    @Mapping(target = "file.id", source = "fileId")
    TaskCommentAttachment formToEntity(TaskCommentAttachmentEditorForm form);

    @Override
    @Mapping(target = "comment.id", source = "commentId")
    @Mapping(target = "file.id", source = "fileId")
    void formToEntity(TaskCommentAttachmentEditorForm form, @org.mapstruct.MappingTarget TaskCommentAttachment entity);
}
