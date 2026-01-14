package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.basic.BasicDocumentDTO;
import com.tnh.baseware.core.dtos.basic.BasicTaskDTO;
import com.tnh.baseware.core.dtos.task.TaskDocumentDTO;
import com.tnh.baseware.core.entities.doc.Document;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskDocument;
import com.tnh.baseware.core.forms.task.TaskDocumentEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.doc.IDocumentRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.components.GenericEntityFetcher;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskDocumentMapper extends IGenericMapper<TaskDocument, TaskDocumentEditorForm, TaskDocumentDTO> {

        @Mapping(target = "task", expression = "java(fetcher.formToEntity(taskRepository, form.getTaskId()))")
        @Mapping(target = "document", expression = "java(fetcher.formToEntity(documentRepository, form.getDocumentId()))")
        TaskDocument formToEntity(TaskDocumentEditorForm form,
                        @Context GenericEntityFetcher fetcher,
                        @Context ITaskRepository taskRepository,
                        @Context IDocumentRepository documentRepository);

        @Mapping(target = "task", expression = "java(fetcher.formToEntity(taskRepository, form.getTaskId()))")
        @Mapping(target = "document", expression = "java(fetcher.formToEntity(documentRepository, form.getDocumentId()))")
        void updateFromForm(TaskDocumentEditorForm form,
                        @MappingTarget TaskDocument entity,
                        @Context GenericEntityFetcher fetcher,
                        @Context ITaskRepository taskRepository,
                        @Context IDocumentRepository documentRepository);

        @Mapping(source = "task", target = "task", qualifiedByName = "mapTask")
        @Mapping(source = "document", target = "document", qualifiedByName = "mapDocument")
        TaskDocumentDTO entityToDTO(TaskDocument entity);

        @Named("mapTask")
        default BasicTaskDTO mapTask(Task task) {
                return task == null ? null
                                : BasicTaskDTO.builder()
                                                .id(task.getId())
                                                .title(task.getTitle())
                                                .description(task.getDescription())
                                                .build();
        }

        @Named("mapDocument")
        default BasicDocumentDTO mapDocument(Document document) {
                return document == null ? null
                                : BasicDocumentDTO.builder()
                                                .id(document.getId())
                                                .documentNumber(document.getDocumentNumber())
                                                .summary(document.getSummary())
                                                .build();
        }
}
