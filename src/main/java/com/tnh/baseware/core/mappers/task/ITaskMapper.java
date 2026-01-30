package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.task.TaskDTO;
import com.tnh.baseware.core.entities.task.Task;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.task.MemberStatus;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.forms.task.TaskEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import com.tnh.baseware.core.repositories.task.ITaskCategoryRepository;
import com.tnh.baseware.core.repositories.task.ITaskListRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;

import java.util.Optional;

import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ITaskMapper extends IGenericMapper<Task, TaskEditorForm, TaskDTO> {

        @Mapping(target = "taskList", expression = "java(form.getTaskListId() != null ? fetcher.formToEntity(taskListRepository, form.getTaskListId()) : null)")
        @Mapping(target = "taskCategory", expression = "java(form.getTaskCategoryId() != null ? fetcher.formToEntity(taskCategoryRepository, form.getTaskCategoryId()) : null)")
        Task formToEntity(TaskEditorForm form,
                        @Context GenericEntityFetcher fetcher,
                        @Context ITaskListRepository taskListRepository,
                        @Context ITaskCategoryRepository taskCategoryRepository);

        @Mapping(target = "taskList", expression = "java(form.getTaskListId() != null ? fetcher.formToEntity(taskListRepository, form.getTaskListId()) : null)")
        @Mapping(target = "taskCategory", expression = "java(form.getTaskCategoryId() != null ? fetcher.formToEntity(taskCategoryRepository, form.getTaskCategoryId()) : null)")
        void updateFromForm(TaskEditorForm form,
                        @MappingTarget Task task,
                        @Context GenericEntityFetcher fetcher,
                        @Context ITaskListRepository taskListRepository,
                        @Context ITaskCategoryRepository taskCategoryRepository);

        @Mapping(target = "memberStatus", expression = "java(getMemberStatus(entity, currentUser, taskMemberRepository))")
        @Mapping(target = "memberRole", expression = "java(getMemberRole(entity, currentUser, taskMemberRepository))")
        TaskDTO entityToDTO(Task entity, @Context User currentUser,
                        @Context ITaskMemberRepository taskMemberRepository);

        default MemberStatus getMemberStatus(Task task, @Context User currentUser,
                        @Context ITaskMemberRepository taskMemberRepository) {
                Optional<TaskMember> taskMember = taskMemberRepository.findByTask_IdAndUser_Id(task.getId(),
                                currentUser.getId());
                if (taskMember.isEmpty()) {
                        return null;
                }
                return taskMember.get().getStatus();
        }

        default TaskMemberRole getMemberRole(Task task, @Context User currentUser,
                        @Context ITaskMemberRepository taskMemberRepository) {
                Optional<TaskMember> taskMember = taskMemberRepository.findByTask_IdAndUser_Id(task.getId(),
                                currentUser.getId());
                if (taskMember.isEmpty()) {
                        return null;
                }
                return taskMember.get().getRole();
        }
}
