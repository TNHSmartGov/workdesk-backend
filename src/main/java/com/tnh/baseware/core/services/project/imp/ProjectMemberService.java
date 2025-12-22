package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.dtos.project.ProjectMemberDTO;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.forms.project.ProjectMemberEditorForm;
import com.tnh.baseware.core.mappers.project.IProjectMemberMapper;
import com.tnh.baseware.core.repositories.project.IProjectMemberRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectMemberService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectMemberService extends GenericService<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO, IProjectMemberRepository, IProjectMemberMapper, UUID> implements IProjectMemberService {

    public ProjectMemberService(IProjectMemberRepository repository, IProjectMemberMapper mapper, MessageService messageService) {
        super(repository, mapper, messageService, ProjectMember.class);
    }
}
