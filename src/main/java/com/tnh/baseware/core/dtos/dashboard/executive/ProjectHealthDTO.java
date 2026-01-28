package com.tnh.baseware.core.dtos.dashboard.executive;

import com.tnh.baseware.core.enums.project.ProjectStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectHealthDTO {
    UUID projectId;
    String projectCode;
    String projectName;
    ProjectStatus status;
    int progress; // 0-100
    long totalTasks;
    long completedTasks;
}
