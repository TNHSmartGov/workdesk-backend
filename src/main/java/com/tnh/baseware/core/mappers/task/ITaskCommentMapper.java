package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.dtos.task.TaskCommentDTO;
import com.tnh.baseware.core.entities.task.TaskComment;
import com.tnh.baseware.core.forms.task.TaskCommentEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskCommentMapper extends IGenericMapper<TaskComment, TaskCommentEditorForm, TaskCommentDTO> {

    @Override
    @Mapping(source = "parentComment.id", target = "parentId")
    TaskCommentDTO entityToDTO(TaskComment entity);

    @Override
    @Mapping(target = "task.id", source = "taskId")
    @Mapping(target = "user.id", source = "userId")
    TaskComment formToEntity(TaskCommentEditorForm form);

    default List<TaskCommentDTO> mapToTree(List<TaskComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        var parentMap = comments.stream()
                .filter(c -> c.getParentComment() != null)
                .collect(Collectors.groupingBy(c -> c.getParentComment().getId()));

        return comments.stream()
                .filter(c -> c.getParentComment() == null)
                .sorted(Comparator.comparing(TaskComment::getCreatedDate))
                .map(c -> buildTree(c, parentMap))
                .toList();
    }

    default TaskCommentDTO buildTree(TaskComment comment,
            Map<UUID, List<TaskComment>> parentMap) {
        var dto = entityToDTO(comment);
        var children = parentMap.getOrDefault(comment.getId(), List.of());

        if (!children.isEmpty()) {
            var childDTOs = children.stream()
                    .sorted(Comparator.comparing(TaskComment::getCreatedDate))
                    .map(child -> buildTree(child, parentMap))
                    .toList();
            dto.setReplies(childDTOs);
        }

        return dto;
    }
}
