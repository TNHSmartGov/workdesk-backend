package com.tnh.baseware.core.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "baseware.organization-stats")
public class OrganizationStatsSchedulerConfig {

    private boolean enabled = true;

    private List<ScheduleConfig> schedules = new ArrayList<>();

    @Data
    public static class ScheduleConfig {

        private String name;

        private String cron;

        private String snapshotType;

        private Boolean enabled;

        public boolean isEnabled(boolean parentEnabled) {
            return enabled != null ? enabled : parentEnabled;
        }
    }

    public List<ScheduleConfig> getEnabledSchedules() {
        return schedules.stream()
                .filter(schedule -> schedule.isEnabled(this.enabled))
                .toList();
    }
}
