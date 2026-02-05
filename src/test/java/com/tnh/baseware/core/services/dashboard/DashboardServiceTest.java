package com.tnh.baseware.core.services.dashboard;

import com.tnh.baseware.core.dtos.dashboard.UnitPerformanceDTO;
import com.tnh.baseware.core.dtos.dashboard.UnitWorkloadDTO;
import com.tnh.baseware.core.enums.task.TaskMemberRole;
import com.tnh.baseware.core.enums.task.TaskStatus;
import com.tnh.baseware.core.repositories.project.IProjectRepository;
import com.tnh.baseware.core.repositories.task.ITaskActivityLogRepository;
import com.tnh.baseware.core.repositories.task.ITaskMemberRepository;
import com.tnh.baseware.core.repositories.task.ITaskRepository;
import com.tnh.baseware.core.repositories.user.IUserOrganizationRepository;
import com.tnh.baseware.core.repositories.stats.IOrganizationDailyStatsRepository;
import com.tnh.baseware.core.services.dashboard.imp.DashboardService;
import com.tnh.baseware.core.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    ITaskRepository taskRepository;
    @Mock
    IProjectRepository projectRepository;
    @Mock
    ITaskActivityLogRepository activityLogRepository;
    @Mock
    IUserOrganizationRepository userOrganizationRepository;
    @Mock
    ITaskMemberRepository taskMemberRepository;
    @Mock
    IOrganizationDailyStatsRepository dailyStatsRepository;
    @Mock
    SecurityUtils securityUtils;

    @InjectMocks
    DashboardService dashboardService;

    UUID orgId;
    UUID userId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void testGetUnitPerformance_WithDates() {
        Instant from = Instant.parse("2023-01-01T00:00:00Z");
        Instant to = Instant.parse("2023-01-31T23:59:59Z");

        when(dailyStatsRepository.aggregatePerformance(eq(orgId), eq(from), eq(to))).thenReturn(null);
        when(taskRepository.countByOrganizationIdTimeboxed(eq(orgId), eq(from), eq(to))).thenReturn(10L);
        when(taskRepository.countByOrganizationIdAndStatusFinishedTimeboxed(eq(orgId), eq(TaskStatus.DONE), eq(from),
                eq(to))).thenReturn(5L);
        when(taskRepository.countByOrganizationIdOverdueTimeboxed(eq(orgId), any(), eq(from), eq(to))).thenReturn(2L);

        UnitPerformanceDTO result = dashboardService.getUnitPerformance(orgId, from, to);

        assertNotNull(result);
        verify(taskRepository).countByOrganizationIdTimeboxed(eq(orgId), eq(from), eq(to));
    }

    @Test
    void testGetUnitPerformance_WithoutDates() {
        // Should default to EPOCH and 2100
        when(taskRepository.countByOrganizationIdTimeboxed(eq(orgId), any(), any())).thenReturn(10L);
        when(taskRepository.countByOrganizationIdAndStatusFinishedTimeboxed(eq(orgId), eq(TaskStatus.DONE), any(),
                any())).thenReturn(5L);
        when(taskRepository.countByOrganizationIdOverdueTimeboxed(eq(orgId), any(), any(), any())).thenReturn(2L);

        UnitPerformanceDTO result = dashboardService.getUnitPerformance(orgId, null, null);

        assertNotNull(result);
        verify(taskRepository).countByOrganizationIdTimeboxed(eq(orgId), eq(Instant.EPOCH),
                eq(Instant.parse("2100-01-01T00:00:00Z")));
    }

    @Test
    void testGetUnitWorkload_WithDates() {
        Instant from = Instant.parse("2023-01-01T00:00:00Z");
        Instant to = Instant.parse("2023-01-31T23:59:59Z");

        when(taskMemberRepository.getWorkloadDistributionTimeboxed(eq(orgId), any(), any(), any(), eq(from), eq(to)))
                .thenReturn(Collections.emptyList());

        List<UnitWorkloadDTO> result = dashboardService.getUnitWorkload(orgId, from, to);

        assertNotNull(result);
        verify(taskMemberRepository).getWorkloadDistributionTimeboxed(eq(orgId), any(), any(), any(), eq(from), eq(to));
    }

    @Test
    void testGetUnitWorkload_WithoutDates() {
        when(taskMemberRepository.getWorkloadDistributionTimeboxed(eq(orgId), any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<UnitWorkloadDTO> result = dashboardService.getUnitWorkload(orgId, null, null);

        assertNotNull(result);
        verify(taskMemberRepository).getWorkloadDistributionTimeboxed(eq(orgId), any(), any(), any(), eq(Instant.EPOCH),
                eq(Instant.parse("2100-01-01T00:00:00Z")));
    }
}
