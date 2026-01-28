package com.tnh.baseware.core.services.dashboard;

import com.tnh.baseware.core.dtos.dashboard.executive.ExecutiveActionItemDTO;
import com.tnh.baseware.core.dtos.dashboard.executive.ExecutiveHotspotDTO;
import com.tnh.baseware.core.dtos.dashboard.executive.ProjectHealthDTO;
import com.tnh.baseware.core.dtos.dashboard.executive.ResourceHealthDTO;
import com.tnh.baseware.core.dtos.dashboard.executive.WeekVelocityDTO;

import java.util.List;

public interface IExecutiveDashboardService {
    ExecutiveHotspotDTO getHotspots();

    ExecutiveActionItemDTO getActionItems();

    List<WeekVelocityDTO> getVelocity();

    List<ResourceHealthDTO> getResourceHealth();

    List<ProjectHealthDTO> getProjectHealth();
}
