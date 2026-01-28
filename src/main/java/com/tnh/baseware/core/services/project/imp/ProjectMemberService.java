package com.tnh.baseware.core.services.project.imp;

import com.tnh.baseware.core.dtos.project.ProjectMemberDTO;
import com.tnh.baseware.core.entities.project.ProjectMember;
import com.tnh.baseware.core.entities.user.UserOrganization;
import com.tnh.baseware.core.enums.project.ProjectPermission;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.forms.project.ProjectMemberEditorForm;
import com.tnh.baseware.core.mappers.project.IProjectMemberMapper;
import com.tnh.baseware.core.repositories.project.IProjectMemberRepository;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.user.IUserOrganizationRepository;
import com.tnh.baseware.core.securities.ProjectSecurityService;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.project.IProjectMemberService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectMemberService extends
        GenericService<ProjectMember, ProjectMemberEditorForm, ProjectMemberDTO, IProjectMemberRepository, IProjectMemberMapper, UUID>
        implements IProjectMemberService {

    IUserOrganizationRepository userOrganizationRepository;
    ProjectSecurityService projectSecurityService;
    IProjectRepository projectRepository;

    public ProjectMemberService(IProjectMemberRepository repository,
            IUserOrganizationRepository userOrganizationRepository,
            IProjectMemberMapper mapper,
            IProjectRepository projectRepository,
            ProjectSecurityService projectSecurityService,
            MessageService messageService) {
        super(repository, mapper, messageService, ProjectMember.class);
        this.userOrganizationRepository = userOrganizationRepository;
        this.projectSecurityService = projectSecurityService;
        this.projectRepository = projectRepository;
    }

    @Override
    public List<ProjectMemberDTO> getMembersByProject(UUID projectId) {
        // lấy người dùng của dự án cụ thể , nếu người dùng đó là quản lý của đơn vị ,
        var curentUser = getCurrentUser();
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BWCBusinessException(messageService.getMessage("project.not.found")));
        var isUserSystem = isUserSystem();

        // lấy danh sách phòng ban của người dùng
        Set<UserOrganization> userOrgs = userOrganizationRepository.findByUserIdAndActiveTrue(curentUser.getId());
        boolean isMemberWithPermission = projectSecurityService.checkPermission(
                curentUser,
                project,
                ProjectPermission.MEMBER_VIEW,
                userOrgs);

        if (!isUserSystem) {
            if (!isMemberWithPermission) {
                throw new BWCBusinessException(messageService.getMessage("project.member.not.found"));
            }
        }

        var members = repository.findDistinctByProject_Id(projectId);
        return members.stream().map(mapper::entityToDTO).toList();
    }

    @Override
    public List<ProjectMemberDTO> getMembersByUser(UUID userId) {
        var members = repository.findDistinctByUser_Id(userId);
        return members.stream().map(mapper::entityToDTO).toList();
    }

}
