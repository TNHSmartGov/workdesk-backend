package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.basic.BasicTaskCategoryDTO;
import com.tnh.baseware.core.dtos.task.TaskCategoryDTO;
import com.tnh.baseware.core.entities.task.TaskCategory;
import com.tnh.baseware.core.forms.task.TaskCategoryEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.task.ITaskCategoryRepository;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskCategoryMapper extends IGenericMapper<TaskCategory, TaskCategoryEditorForm, TaskCategoryDTO> {

        // Form → Entity (for CREATE)
        @Mapping(target = "parent", expression = "java(form.getParentId() != null ? fetcher.formToEntity(repository, form.getParentId()) : null)")
        TaskCategory formToEntity(TaskCategoryEditorForm form,
                        @Context GenericEntityFetcher fetcher,
                        @Context ITaskCategoryRepository repository);

        BasicTaskCategoryDTO entityToBasicDTO(TaskCategory entity);

        // Form → Entity (for UPDATE)
        @Mapping(target = "parent", expression = "java(form.getParentId() != null ? fetcher.formToEntity(repository, form.getParentId()) : null)")
        void updateFromForm(TaskCategoryEditorForm form,
                        @MappingTarget TaskCategory entity,
                        @Context GenericEntityFetcher fetcher,
                        @Context ITaskCategoryRepository repository);

        // Entity → DTO
        @Mapping(source = "parent", target = "parent", qualifiedByName = "mapParent")
        TaskCategoryDTO entityToDTO(TaskCategory entity);

        @Named("mapParent")
        default TaskCategoryDTO mapParent(TaskCategory parent) {
                return parent == null ? null
                                : TaskCategoryDTO.builder()
                                                .id(parent.getId())
                                                .name(parent.getName())
                                                .code(parent.getCode())
                                                .orderIndex(parent.getOrderIndex())
                                                .description(parent.getDescription())
                                                .build();
        }

        default List<TaskCategoryDTO> mapTaskCategoriesToTree(List<TaskCategory> categories) {
                if (categories == null || categories.isEmpty()) {
                        return List.of();
                }

                var parentMap = categories.stream()
                                .filter(c -> c.getParent() != null)
                                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

                return categories.stream()
                                .filter(c -> c.getParent() == null)
                                .sorted((c1, c2) -> Integer.compare(c1.getOrderIndex(), c2.getOrderIndex()))
                                .map(c -> buildTaskCategoryTree(c, parentMap))
                                .toList();
        }

        default TaskCategoryDTO buildTaskCategoryTree(TaskCategory category, Map<UUID, List<TaskCategory>> parentMap) {
                var dto = entityToDTO(category);
                var children = parentMap.getOrDefault(category.getId(), List.of());

                if (!children.isEmpty()) {
                        var childDTOs = children.stream()
                                        .sorted((c1, c2) -> Integer.compare(c1.getOrderIndex(), c2.getOrderIndex()))
                                        .map(child -> buildTaskCategoryTree(child, parentMap))
                                        .toList();
                        dto.setChildren(childDTOs);
                }

                return dto;
        }

}
