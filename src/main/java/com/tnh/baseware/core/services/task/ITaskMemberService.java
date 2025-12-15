package com.tnh.baseware.core.services.task;

import com.tnh.baseware.core.dtos.task.TaskMemberDTO;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.forms.task.TaskMemberEditorForm;
import com.tnh.baseware.core.services.IGenericService;

import java.util.UUID;

public interface ITaskMemberService extends IGenericService<TaskMember, TaskMemberEditorForm, TaskMemberDTO, UUID> {
}
