package com.tnh.baseware.core.services.user.imp;

import com.tnh.baseware.core.dtos.user.UserProfileDTO;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.entities.user.UserProfile;
import com.tnh.baseware.core.enums.project.ProjectStatus;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.exceptions.BWCBusinessException;
import com.tnh.baseware.core.forms.user.UserProfileForm;
import com.tnh.baseware.core.repositories.stats.UserStatsCalculationRepository;
import com.tnh.baseware.core.repositories.user.IUserProfileRepository;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.storage.IStorageService;
import com.tnh.baseware.core.services.user.IUserProfileService;
import com.tnh.baseware.core.services.user.IUserService;
import com.tnh.baseware.core.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileService implements IUserProfileService {

    IUserProfileRepository profileRepository;
    UserStatsCalculationRepository statsCalculationRepository;
    IUserRepository userRepository;
    IUserService userService;
    MessageService messageService;
    SecurityUtils securityUtils;
    @Qualifier("s3StorageService")
    IStorageService<String> storageService;

    @Override
    @Transactional
    public UserProfileDTO getProfile(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("user.not.found", userId)));

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createInitialProfile(user));

        return mapToDTO(user, profile);
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(UUID userId, UserProfileForm form) {
        var currentUser = securityUtils.currentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new BWCBusinessException(messageService.getMessage("user.not.allowed"));
        }
        userService.editProfile(userId, form);

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createInitialProfile(userRepository.findById(userId).orElseThrow()));

        if (form.getDescription() != null) {
            profile.setDescription(form.getDescription());
            profileRepository.save(profile);
        }

        return getProfile(userId);
    }

    @Override
    @Transactional
    public String updateAvatar(UUID userId, MultipartFile file) {
        var currentUser = securityUtils.currentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new BWCBusinessException(messageService.getMessage("user.not.allowed"));
        }
        String path = storageService.uploadFile(file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("user.not.found", userId)));
        user.setAvatarUrl(path);
        userRepository.save(user);
        return path;
    }

    @Override
    @Transactional
    public String updateCover(UUID userId, MultipartFile file) {
        var currentUser = securityUtils.currentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new BWCBusinessException(messageService.getMessage("user.not.allowed"));
        }
        String path = storageService.uploadFile(file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("user.not.found", userId)));

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createInitialProfile(user));
        profile.setCoverUrl(path);
        profileRepository.save(profile);
        return path;
    }

    @Override
    @Transactional
    public void refreshUserStats(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(messageService.getMessage("user.not.found", userId)));
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> createInitialProfile(user));

        Integer totalTasks = statsCalculationRepository.countTotalTasksByUserId(userId);
        Integer participated = statsCalculationRepository.countParticipatedTasksByUserId(userId);
        Integer completedTasks = statsCalculationRepository.countCompletedTasksByUserId(userId, TaskStatus.DONE);
        Integer completedProjects = statsCalculationRepository.countCompletedProjectsByUserId(userId,
                ProjectStatus.COMPLETED);

        profile.setTotalTasks(totalTasks != null ? totalTasks : 0);
        profile.setParticipatedTasks(participated != null ? participated : 0);
        profile.setCompletedTasks(completedTasks != null ? completedTasks : 0);
        profile.setCompletedProjects(completedProjects != null ? completedProjects : 0);

        if (profile.getTotalTasks() > 0) {
            double perf = (double) profile.getCompletedTasks() / profile.getTotalTasks() * 100.0;
            profile.setPerformance((double) Math.round(perf * 100) / 100);
        } else {
            profile.setPerformance(0.0);
        }

        profile.setLastStatsUpdate(Instant.now());
        profileRepository.save(profile);
    }

    private UserProfile createInitialProfile(User user) {
        UserProfile profile = UserProfile.builder()
                .user(user)
                .joinDate(user.getCreatedDate() != null ? user.getCreatedDate() : Instant.now())
                .totalTasks(0)
                .participatedTasks(0)
                .completedProjects(0)
                .completedTasks(0)
                .performance(0.0)
                .build();
        return profileRepository.save(profile);
    }

    private UserProfileDTO mapToDTO(User user, UserProfile profile) {
        return UserProfileDTO.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthday(user.getBirthday())
                .address(user.getAddress())
                .idn(user.getIdn())
                .ial(user.getIal())
                .avatarUrl(user.getAvatarUrl())
                .coverUrl(profile.getCoverUrl())
                .joinDate(profile.getJoinDate())
                .description(profile.getDescription())
                .totalTasks(profile.getTotalTasks())
                .participatedTasks(profile.getParticipatedTasks())
                .completedTasks(profile.getCompletedTasks())
                .completedProjects(profile.getCompletedProjects())
                .performance(profile.getPerformance())
                .lastStatsUpdate(profile.getLastStatsUpdate())
                .build();
    }
}
