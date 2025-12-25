package com.tnh.baseware.core.services.user.imp;

import com.tnh.baseware.core.components.GenericEntityFetcher;
import com.tnh.baseware.core.dtos.user.UserDTO;
import com.tnh.baseware.core.dtos.user.UserTokenDTO;
import com.tnh.baseware.core.entities.user.Menu;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.entities.user.UserOrganization;
import com.tnh.baseware.core.events.type.UserCreatedEvent;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.forms.user.ChangePasswordForm;
import com.tnh.baseware.core.forms.user.RegisterForm;
import com.tnh.baseware.core.forms.user.ResetPasswordForm;
import com.tnh.baseware.core.forms.user.UserEditorForm;
import com.tnh.baseware.core.forms.user.UserProfileForm;
import com.tnh.baseware.core.mappers.audit.ICategoryMapper;
import com.tnh.baseware.core.mappers.user.IMenuMapper;
import com.tnh.baseware.core.mappers.user.IUserMapper;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.repositories.adu.IOrganizationRepository;
import com.tnh.baseware.core.repositories.user.IMenuRepository;
import com.tnh.baseware.core.repositories.user.IRoleRepository;
import com.tnh.baseware.core.repositories.user.IUserOrganizationRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.securities.JwtTokenService;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.user.IUserService;
import com.tnh.baseware.core.utils.BasewareUtils;
import com.tnh.baseware.core.utils.LogStyleHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserService extends GenericService<User, UserEditorForm, UserDTO, IUserRepository, IUserMapper, UUID>
        implements IUserService {

    IRoleRepository roleRepository;
    IOrganizationRepository organizationRepository;
    IMenuRepository menuRepository;
    PasswordEncoder passwordEncoder;
    JwtTokenService jwtTokenService;
    SecurityProperties securityProperties;
    IMenuMapper menuMapper;
    ICategoryMapper categoryMapper;
    IUserOrganizationRepository userOrganizationRepository;
    ApplicationEventPublisher eventPublisher;

    public UserService(IUserRepository repository,
                       IUserMapper mapper,
                       MessageService messageService,
                       IRoleRepository roleRepository,
                       IOrganizationRepository organizationRepository,
                       IMenuRepository menuRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService,
                       SecurityProperties securityProperties,
                       ICategoryMapper categoryMapper,
                       IUserOrganizationRepository userOrganizationRepository,
                       IMenuMapper menuMapper,
                       ApplicationEventPublisher eventPublisher) {
        super(repository, mapper, messageService, User.class);
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.menuRepository = menuRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.securityProperties = securityProperties;
        this.menuMapper = menuMapper;
        this.userOrganizationRepository = userOrganizationRepository;
        this.categoryMapper = categoryMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UserDTO create(UserEditorForm form) {
        var user = mapper.formToEntity(form, passwordEncoder);
        User savedUser = repository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(savedUser.getId(), savedUser.getUsername()));
        return mapper.entityToDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO update(UUID id, UserEditorForm form) {
        var user = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found", id)));
        mapper.updateUserFromForm(form, user);
        return mapper.entityToDTO(repository.save(user));
    }

    @Override
    @Transactional
    public UserDTO registerUser(RegisterForm form, HttpServletRequest request) {
        if (repository.existsByUsernameOrPhoneOrEmailOrIdn(form.getUsername(), form.getPhone(), form.getEmail(),
                form.getIdn())) {
            throw new BWCValidationException(messageService.getMessage("user.already.exists"));
        }

        var user = User.builder()
                .username(form.getUsername())
                .password(passwordEncoder.encode(form.getPassword()))
                .firstName(form.getFirstName())
                .lastName(form.getLastName())
                .fullName(form.getFullName())
                .phone(form.getPhone())
                .email(form.getEmail())
                .avatarUrl(form.getAvatarUrl())
                .idn(form.getIdn())
                .ial(form.getIal())
                .enabled(securityProperties.getRegister().isEnabled())
                .build();

        var roles = roleRepository.findAllByField("name", securityProperties.getRegister().getRoleDefault());
        var role = BasewareUtils.isBlank(roles) ? null : roles.getFirst();

        if (BasewareUtils.isBlank(role)) {
            throw new BWCNotFoundException(messageService.getMessage("role.not.found"));
        }

        user.addRole(role);
        return mapper.entityToDTO(repository.save(user));
    }

    @Override
    @Transactional
    public UserDTO editProfile(UUID id, UserProfileForm form) {
        var user = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found", id)));

        if (!BasewareUtils.isBlank(form.getPhone()) && !form.getPhone().equals(user.getPhone())) {
            if (repository.existsByPhone(form.getPhone())) {
                throw new BWCValidationException(messageService.getMessage("user.phone.already.exists"));
            }
            user.setPhone(form.getPhone());
        }

        if (!BasewareUtils.isBlank(form.getEmail()) && !form.getEmail().equals(user.getEmail())) {
            if (repository.existsByEmail(form.getEmail())) {
                throw new BWCValidationException(messageService.getMessage("user.email.already.exists"));
            }
            user.setEmail(form.getEmail());
        }

        if (!BasewareUtils.isBlank(form.getIdn()) && !form.getIdn().equals(user.getIdn())) {
            if (repository.existsByIdn(form.getIdn())) {
                throw new BWCValidationException(messageService.getMessage("user.idn.already.exists"));
            }
            user.setIdn(form.getIdn());
        }

        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setFullName(form.getFullName());
        user.setAvatarUrl(form.getAvatarUrl());
        user.setIal(form.getIal());
        return mapper.entityToDTO(repository.save(user));
    }

    @Override
    @Transactional
    public void enableUser(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found", id)));
        user.setEnabled(true);
        repository.save(user);
    }

    @Override
    @Transactional
    public void disableUser(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found", id)));
        user.setEnabled(false);
        repository.save(user);
    }

    @Override
    @Transactional
    public void lockUser(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found", id)));
        user.setLocked(true);
        repository.save(user);
    }

    @Override
    @Transactional
    public void unlockUser(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found", id)));
        user.setLocked(false);
        repository.save(user);
    }

    @Override
    @Transactional
    public void resetFailedLoginAttempts(UUID id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found", id)));
        user.setFailedLoginAttempts(0);
        repository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID id, ChangePasswordForm form) {
        var user = repository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found", id)));
        validatePasswordChange(user, form);

        user.setPassword(passwordEncoder.encode(form.getPasswordNew()));
        repository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordForm form) {
        var user = repository.findById(form.getUserId())
                .orElseThrow(
                        () -> new BWCNotFoundException(messageService.getMessage("user.not.found", form.getUserId())));
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        repository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserTokenDTO findByToken(String token) {
        if (BasewareUtils.isBlank(token)) {
            log.debug(LogStyleHelper.debug("Token is null or blank, skipping user retrieval"));
            throw new BWCValidationException(messageService.getMessage("token.blank"));
        }

        var username = jwtTokenService.extractUsername(token)
                .orElseThrow(() -> new BWCValidationException(messageService.getMessage("token.invalid")));

        var user = repository.findByUsername(username)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("user.not.found", username)));

        List<Menu> menus;
        if (Boolean.TRUE.equals(user.getSuperAdmin())) {
            menus = menuRepository.findAll();
        } else {
            menus = user.getRoles().stream()
                    .flatMap(role -> role.getMenus().stream())
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Menu::getId))),
                            ArrayList::new));
        }

        if (BasewareUtils.isBlank(menus)) {
            log.debug(LogStyleHelper.debug("User {} has no menus"), username);
            throw new BWCNotFoundException(messageService.getMessage("user.menus.not.found", username));
        }

        return UserTokenDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .idn(user.getIdn())
                .ial(user.getIal())
                .enabled(user.getEnabled())
                .locked(user.getLocked())
                .lockTime(user.getLockTime())
                .accountExpiryDate(user.getAccountExpiryDate())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .superAdmin(user.getSuperAdmin())
                .menus(menuMapper.entitiesToDTOs(menus))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAllByOrganization(UUID id) {
        var organization = organizationRepository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("organization.not.found", id)));
        Set<UserOrganization> userOrgs = userOrganizationRepository
                .findByOrganizationIdAndActiveTrue(organization.getId());
        List<UserDTO> usersDTO = userOrgs.stream()
                .map(UserOrganization::getUser)
                .map(mapper::entityToDTO)
                .toList();
        for (int i = 0; i < usersDTO.size(); i++) {
            usersDTO.get(i).setLevel(
                    categoryMapper.entityToDTO(userOrgs.toArray(new UserOrganization[0])[i].getTitle()));
        }
        return usersDTO;
    }

    @Override
    @Transactional
    public Page<UserDTO> findAllByOrganization(UUID id, Pageable pageable) {
        var organization = organizationRepository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("organization.not.found", id)));
        Page<UserOrganization> userOrgs = userOrganizationRepository
                .findByOrganizationIdAndActiveTrue(organization.getId(), pageable);
        List<UserDTO> usersDTO = userOrgs.stream()
                .map(UserOrganization::getUser)
                .map(mapper::entityToDTO)
                .toList();
        // set title for each userDTO
        for (int i = 0; i < usersDTO.size(); i++) {
            usersDTO.get(i).setLevel(
                    categoryMapper.entityToDTO(userOrgs.getContent().get(i).getTitle()));
        }
        return PageableExecutionUtils.getPage(
                usersDTO,
                pageable,
                userOrgs::getTotalElements);

    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAllWithoutOrganization(UUID id) {
        var organization = organizationRepository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("organization.not.found", id)));
        return repository.findAllNotInOrganization(organization.getId())
                .stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAllWithoutOrganization(UUID id, Pageable pageable) {
        var organization = organizationRepository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("organization.not.found", id)));
        return repository.findAllByEntitiesNotContaining("organizations", organization, pageable)
                .map(mapper::entityToDTO);

    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAllWithoutRole(UUID id) {
        var role = roleRepository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("role.not.found", id)));
        return repository.findAllByEntitiesNotContaining("roles", role)
                .stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAllWithoutRole(UUID id, Pageable pageable) {
        var role = roleRepository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("role.not.found", id)));
        return repository.findAllByEntitiesNotContaining("roles", role, pageable).map(mapper::entityToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAllByRole(UUID id) {
        var role = roleRepository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("role.not.found", id)));
        return repository.findAllByEntitiesContaining("roles", role)
                .stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAllByRole(UUID id, Pageable pageable) {
        var role = roleRepository.findById(id)
                .orElseThrow(() -> new BWCNotFoundException(messageService.getMessage("role.not.found", id)));
        return repository.findAllByEntitiesContaining("roles", role, pageable).map(mapper::entityToDTO);
    }

    private void validatePasswordChange(User user, ChangePasswordForm form) {
        if (!passwordEncoder.matches(form.getPasswordOld(), user.getPassword())) {
            throw new BWCValidationException(messageService.getMessage("auth.password.not.match"));
        }
        if (!form.getPasswordNew().equals(form.getPasswordNewConfirm())) {
            throw new BWCValidationException(messageService.getMessage("auth.password.not.match"));
        }
        if (passwordEncoder.matches(form.getPasswordNew(), user.getPassword())) {
            throw new BWCValidationException(messageService.getMessage("auth.password.same"));
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BWCValidationException(messageService.getMessage("file.empty"));
        }
        // checking file type xlsx , xls
        String contentType = file.getContentType();
        if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) &&
                !"application/vnd.ms-excel".equals(contentType)) {
            throw new BWCValidationException(messageService.getMessage("file.invalid.type"));

        }
    }

}
