package com.tnh.baseware.core.services.user;

import com.tnh.baseware.core.dtos.user.UserDTO;
import com.tnh.baseware.core.dtos.user.UserTokenDTO;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.forms.user.ChangePasswordForm;
import com.tnh.baseware.core.forms.user.RegisterForm;
import com.tnh.baseware.core.forms.user.ResetPasswordForm;
import com.tnh.baseware.core.forms.user.UserEditorForm;
import com.tnh.baseware.core.forms.user.UserProfileForm;
import com.tnh.baseware.core.services.IGenericService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IUserService extends IGenericService<User, UserEditorForm, UserDTO, UUID> {

    @Override
    UserDTO create(UserEditorForm form);

    @Override
    UserDTO update(UUID id, UserEditorForm form);

    UserDTO registerUser(RegisterForm form, HttpServletRequest request);

    UserDTO editProfile(UUID id, UserProfileForm form);

    void enableUser(UUID id);

    void disableUser(UUID id);

    void lockUser(UUID id);

    void unlockUser(UUID id);

    void resetFailedLoginAttempts(UUID id);

    void changePassword(UUID id, ChangePasswordForm form);

    void resetPassword(ResetPasswordForm form);

    UserTokenDTO findByToken(String token);

    List<UserDTO> findAllByOrganization(UUID id);

    Page<UserDTO> findAllByOrganization(UUID id, Pageable pageable);

    List<UserDTO> findAllWithoutOrganization(UUID id);

    Page<UserDTO> findAllWithoutOrganization(UUID id, Pageable pageable);

    List<UserDTO> findAllWithoutRole(UUID id);

    Page<UserDTO> findAllWithoutRole(UUID id, Pageable pageable);

    List<UserDTO> findAllByRole(UUID id);

    Page<UserDTO> findAllByRole(UUID id, Pageable pageable);

}