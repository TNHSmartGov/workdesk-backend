package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskMemberDTO;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.forms.task.TaskMemberEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.List;
import java.util.UUID;

public interface ITaskMemberService extends IGenericService<TaskMember, TaskMemberEditorForm, TaskMemberDTO, UUID> {
    TaskMemberDTO assignMember(UUID taskId, TaskMemberEditorForm form);
    TaskMemberDTO updateMember(UUID taskId, UUID memberId, TaskMemberEditorForm form);
    void removeMember(UUID taskId, UUID memberId);
    List<TaskMemberDTO> getTaskMembers(UUID taskId);
}
