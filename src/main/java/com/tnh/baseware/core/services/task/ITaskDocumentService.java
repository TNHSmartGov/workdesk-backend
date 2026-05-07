package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskDocumentDTO;
import com.tnh.baseware.core.entities.task.TaskDocument;
import com.tnh.baseware.core.forms.task.TaskDocumentEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.List;
import java.util.UUID;

public interface ITaskDocumentService
        extends IGenericService<TaskDocument, TaskDocumentEditorForm, TaskDocumentDTO, UUID> {

    List<TaskDocumentDTO> getDocumentsByTask(UUID taskId);

    List<TaskDocumentDTO> getTasksByDocument(UUID documentId);
}
