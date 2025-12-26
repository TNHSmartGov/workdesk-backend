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

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectMemberService extends
        GenericService<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO, IProjectMemberRepository, IProjectMemberMapper, UUID>
        implements IProjectMemberService {

    public ProjectMemberService(IProjectMemberRepository repository, IProjectMemberMapper mapper,
            MessageService messageService) {
        super(repository, mapper, messageService, ProjectMember.class);
    }

    @Override
    public List<ProjectMemberDTO> getMembersByProject(UUID projectId) {
        // lấy người dùng của dự án cụ thể , nếu người dùng đó là quản lý của đơn vị ,
        // nếu người dùng
        var isUserSystem = isUserSystem();

        var members = repository.findDistinctByProject_Id(projectId);
        return members.stream().map(mapper::entityToDTO).toList();
        // TODO Auto-generated method stub
    }

    @Override
    public List<ProjectMemberDTO> getMembersByUser(UUID userId) {
        var members = repository.findDistinctByUser_Id(userId);
        return members.stream().map(mapper::entityToDTO).toList();
    }
}
