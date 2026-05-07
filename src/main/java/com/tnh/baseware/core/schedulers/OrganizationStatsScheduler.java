package com.tnh.baseware.core.schedulers;

import com.tnh.baseware.core.configs.OrganizationStatsSchedulerConfig;
import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.exceptions.BWCGenericRuntimeException;
import com.tnh.baseware.core.repositories.adu.IOrganizationRepository;
import com.tnh.baseware.core.services.stats.IOrganizationStatsService;
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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Lazy
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationStatsScheduler implements SchedulingConfigurer {

    IOrganizationStatsService statsService;
    IOrganizationRepository organizationRepository;
    OrganizationStatsSchedulerConfig config;

    @PostConstruct
    public void init() {
        if (!config.isEnabled()) {
            log.warn(LogStyleHelper.warn("Organization stats scheduler is DISABLED"));
            return;
        }

        List<OrganizationStatsSchedulerConfig.ScheduleConfig> enabledSchedules = config.getEnabledSchedules();

        if (enabledSchedules.isEmpty()) {
            log.warn(LogStyleHelper.warn("No organization stats schedules configured"));
            return;
        }

        log.info(LogStyleHelper.info("Organization stats scheduler ENABLED with {} schedules:"),
                enabledSchedules.size());

        for (OrganizationStatsSchedulerConfig.ScheduleConfig schedule : enabledSchedules) {
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

        for (OrganizationStatsSchedulerConfig.ScheduleConfig schedule : config.getEnabledSchedules()) {
            try {
                CronExpression.parse(schedule.getCron());

                CronTask task = new CronTask(
                        () -> generateStats(schedule.getName()),
                        schedule.getCron());

                taskRegistrar.addCronTask(task);

                log.debug("Registered schedule: {} with cron: {}",
                        schedule.getName(), schedule.getCron());

            } catch (IllegalArgumentException e) {
                log.error(LogStyleHelper.error("Invalid configuration for schedule '{}': {}"),
                        schedule.getName(), e.getMessage());
            }
        }
    }

    /**
     * Generate stats for all organizations
     * This method is called by each configured schedule
     * 
     * @param scheduleName Name of the schedule (for logging)
     */
    @Async
    public void generateStats(String scheduleName) {
        LocalDate today = LocalDate.now();

        // Skip if today is weekend (extra safety check)
        if (isWeekend(today)) {
            log.debug(LogStyleHelper.debug("Skipping {} stats generation - weekend"), scheduleName);
            return;
        }

        try {
            log.info(LogStyleHelper.info("=== Starting {} stats generation for {} ==="),
                    scheduleName, today);

            List<Organization> organizations = organizationRepository.findAll();
            log.info("Found {} organizations to process", organizations.size());

            int successCount = 0;
            int errorCount = 0;

            // Normalize today to Start of Day (UTC) for the service call
            Instant snapshotTime = today.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant();

            for (Organization org : organizations) {
                try {
                    statsService.calculateAndSaveStats(org.getId(), snapshotTime);
                    successCount++;

                    // Log sample message for first org
                    if (successCount == 1) {
                        log.debug("Successfully calculated stats for org: {}", org.getName());
                    }

                } catch (Exception ex) {
                    errorCount++;
                    log.error(LogStyleHelper.error("Failed to generate stats for organization {}: {}"),
                            org.getName(), ex.getMessage(), ex);
                }
            }

            log.info(LogStyleHelper.info("=== {} stats generation completed: {} success, {} errors ==="),
                    scheduleName, successCount, errorCount);

        } catch (Exception ex) {
            log.error(LogStyleHelper.error("Error during {} stats generation: {}"),
                    scheduleName, ex.getMessage(), ex);
            throw new BWCGenericRuntimeException("Error during " + scheduleName + " stats generation", ex);
        }
    }

    /**
     * Check if a date falls on weekend
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
