package com.tnh.baseware.core.services.task.imp;

import com.tnh.baseware.core.dtos.task.TaskMemberDTO;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.forms.task.TaskMemberEditorForm;
import com.tnh.baseware.core.mappers.task.ITaskMemberMapper;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskMemberService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskMemberService extends GenericService<TaskMember, TaskMemberEditorForm, TaskMemberDTO, ITaskMemberRepository, ITaskMemberMapper, UUID> implements ITaskMemberService {

    public TaskMemberService(ITaskMemberRepository repository, ITaskMemberMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, TaskMember.class);
    }
}
