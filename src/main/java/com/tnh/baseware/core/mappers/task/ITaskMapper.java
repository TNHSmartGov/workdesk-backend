package com.tnh.baseware.core.mappers.task;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.basic.BasicUserDTO;
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

import com.tnh.baseware.core.mappers.task.ITaskAgileInfoMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { ITaskAgileInfoMapper.class })
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
        @Mapping(target = "leader", expression = "java(getMemberLead(entity, taskMemberRepository))")
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

        default BasicUserDTO getMemberLead(Task task, @Context ITaskMemberRepository taskMemberRepository) {
                var members = taskMemberRepository.findByTask_IdAndRole(task.getId(), TaskMemberRole.LEAD);
                // Ưu tiên tìm LEAD
                var lead = members.stream()
                                .findFirst();
                BasicUserDTO basicUserDTO = new BasicUserDTO();
                if (lead.isPresent()) {
                        basicUserDTO.setId(lead.get().getUser().getId());
                        basicUserDTO.setFullName(lead.get().getUser().getFullName());
                        basicUserDTO.setAvatarUrl(lead.get().getUser().getAvatarUrl());
                        return basicUserDTO;
                }
                members = taskMemberRepository.findByTask_IdAndRole(task.getId(), TaskMemberRole.OWNER);
                // Nếu không có LEAD, tìm OWNER
                var owner = members.stream()
                                .findFirst();
                if (owner.isPresent()) {
                        basicUserDTO.setId(owner.get().getUser().getId());
                        basicUserDTO.setFullName(owner.get().getUser().getFullName());
                        basicUserDTO.setAvatarUrl(owner.get().getUser().getAvatarUrl());
                        return basicUserDTO;
                }
                return null;
        }
}
