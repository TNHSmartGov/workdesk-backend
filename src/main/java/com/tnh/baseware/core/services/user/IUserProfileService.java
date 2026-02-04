package com.tnh.baseware.core.services.user;

import com.tnh.baseware.core.dtos.user.UserProfileDTO;
import com.tnh.baseware.core.forms.user.UserProfileForm;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface IUserProfileService {
    UserProfileDTO getProfile(UUID userId);

    UserProfileDTO updateProfile(UUID userId, UserProfileForm form);

    String updateAvatar(UUID userId, MultipartFile file);

    String updateCover(UUID userId, MultipartFile file);

    void refreshUserStats(UUID userId);
}
