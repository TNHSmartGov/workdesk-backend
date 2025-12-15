package com.tnh.baseware.core.resources.task;

import com.tnh.baseware.core.dtos.task.TaskMemberDTO;
import com.tnh.baseware.core.entities.task.TaskMember;
import com.tnh.baseware.core.forms.task.TaskMemberEditorForm;
import com.tnh.baseware.core.properties.SystemProperties;
import com.tnh.baseware.core.resources.GenericResource;
import com.tnh.baseware.core.services.IGenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.task.ITaskMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Task Members", description = "API for managing task members")
@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("${baseware.core.system.api-prefix}/task-members")
public class TaskMemberResource extends GenericResource<TaskMember, TaskMemberEditorForm, TaskMemberDTO, UUID> {

    ITaskMemberService taskMemberService;

    public TaskMemberResource(IGenericService<TaskMember, TaskMemberEditorForm, TaskMemberDTO, UUID> service,
                              MessageService messageService,
                              SystemProperties systemProperties,
                              ITaskMemberService taskMemberService) {
        super(service, messageService, systemProperties.getApiPrefix() + "/task-members");
        this.taskMemberService = taskMemberService;
    }

    @Override
    public TaskMemberDTO toModel(TaskMemberDTO dto) {
        // Since ID is composite, we might not be able to generate a simple self-link easily
        // unless we serialize the ID or use query params.
        // For now, let's just return the DTO without adding Hateoas links that rely on a single string ID
        // or override to handle composite ID stringification.
        return dto;
    }
}
