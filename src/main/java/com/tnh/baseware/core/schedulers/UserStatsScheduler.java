package com.tnh.baseware.core.schedulers;

import com.tnh.baseware.core.configs.UserStatsSchedulerConfig;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.repositories.user.IUserRepository;
import com.tnh.baseware.core.services.user.IUserProfileService;
import com.tnh.baseware.core.utils.LogStyleHelper;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Lazy
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserStatsScheduler implements SchedulingConfigurer {

    IUserRepository userRepository;
    IUserProfileService userProfileService;
    UserStatsSchedulerConfig config;

    @PostConstruct
    public void init() {
        if (!config.isEnabled()) {
            log.warn(LogStyleHelper.warn("User stats scheduler is DISABLED"));
            return;
        }

        List<UserStatsSchedulerConfig.ScheduleConfig> enabledSchedules = config.getEnabledSchedules();

        if (enabledSchedules.isEmpty()) {
            log.warn(LogStyleHelper.warn("No user stats schedules configured"));
            return;
        }

        log.info(LogStyleHelper.info("User stats scheduler ENABLED with {} schedules:"),
                enabledSchedules.size());

        for (UserStatsSchedulerConfig.ScheduleConfig schedule : enabledSchedules) {
            log.info("  - {}: {}",
                    schedule.getName(),
                    schedule.getCron());
        }
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (!config.isEnabled()) {
            return;
        }

        for (UserStatsSchedulerConfig.ScheduleConfig schedule : config.getEnabledSchedules()) {
            try {
                CronExpression.parse(schedule.getCron());

                CronTask task = new CronTask(
                        () -> calculateUserStats(schedule.getName()),
                        schedule.getCron());

                taskRegistrar.addCronTask(task);

                log.debug("Registered user stats schedule: {} with cron: {}",
                        schedule.getName(), schedule.getCron());

            } catch (IllegalArgumentException e) {
                log.error(LogStyleHelper.error("Invalid configuration for user stats schedule '{}': {}"),
                        schedule.getName(), e.getMessage());
            }
        }
    }

    @Async
    @Transactional
    public void calculateUserStats(String scheduleName) {
        log.info("=== Starting {} User Stats Daily Calculation Job at {} ===", scheduleName, LocalDateTime.now());

        List<User> users = userRepository.findAll();

        int count = 0;
        int errorCount = 0;
        for (User user : users) {
            try {
                userProfileService.refreshUserStats(user.getId());
                count++;
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to refresh stats for user {}", user.getId(), e);
            }
        }

        log.info("=== Completed {} User Stats Daily Calculation Job. Processed {} users, {} errors ===", scheduleName,
                count, errorCount);
    }
}
