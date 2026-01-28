package com.tnh.baseware.core.dtos.dashboard.executive;

import com.tnh.baseware.core.dtos.task.TaskDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExecutiveActionItemDTO {
    List<TaskDTO> approvalQueue;
    List<TaskDTO> myUrgentTasks;
}
