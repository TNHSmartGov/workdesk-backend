package com.tnh.baseware.core.services.dashboard;

import com.tnh.baseware.core.dtos.dashboard.executive.ExecutiveActionItemDTO;
import com.tnh.baseware.core.dtos.dashboard.executive.ExecutiveHotspotDTO;
import com.tnh.baseware.core.dtos.dashboard.executive.ProjectHealthDTO;
import com.tnh.baseware.core.dtos.dashboard.executive.ResourceHealthDTO;
import com.tnh.baseware.core.dtos.dashboard.executive.WeekVelocityDTO;

import java.util.List;

public interface IExecutiveDashboardService {
    ExecutiveHotspotDTO getHotspots(java.util.UUID orgId);

    ExecutiveActionItemDTO getActionItems(java.util.UUID orgId, java.util.UUID userId);

    List<WeekVelocityDTO> getVelocity(java.util.UUID orgId);

    List<ResourceHealthDTO> getResourceHealth(java.util.UUID orgId);

    List<ProjectHealthDTO> getProjectHealth(java.util.UUID orgId);
}
