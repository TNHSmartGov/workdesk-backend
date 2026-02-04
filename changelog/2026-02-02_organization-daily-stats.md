# Organization Daily Statistics Feature

**Date:** 2026-02-02  
**Status:** ✅ Complete  
**Developer:** VUONGDH  
**Feature Type:** New Feature

---

## Overview

Hệ thống thống kê hằng ngày cho tổ chức với dual daily snapshots (12PM & 5:30PM). Track 17 core metrics về tasks, projects, và performance. Hỗ trợ flexible extended metrics qua JSONB field.

**Key Features:**
- Dual daily snapshots (MIDDAY & END_OF_DAY)
- 17 core metrics + flexible JSON metrics
- Auto-scheduled jobs (weekdays only)
- REST API with 5 endpoints
- Archive support (2-year retention)

---

## Changes Made

### Files Created

#### Entities & Enums
1. `src/main/java/com/tnh/baseware/core/entities/stats/OrganizationDailyStats.java`
   - Main entity storing daily stats
   - 17 metrics fields + JSONB extendedMetrics
   - Archive support

2. `src/main/java/com/tnh/baseware/core/enums/stats/SnapshotType.java`
   - Enum: MIDDAY, END_OF_DAY

#### Repositories
3. `src/main/java/com/tnh/baseware/core/repositories/stats/IOrganizationDailyStatsRepository.java`
   - Standard CRUD + custom queries
   - Archive management

4. `src/main/java/com/tnh/baseware/core/repositories/stats/IStatsCalculationRepository.java`
   - Native SQL queries for stats calculation
   - High-performance aggregation

5. `src/main/java/com/tnh/baseware/core/repositories/stats/TaskStatsProjection.java`
   - Projection for task metrics

6. `src/main/java/com/tnh/baseware/core/repositories/stats/ProjectStatsProjection.java`
   - Projection for project metrics

#### DTOs & Mappers
7. `src/main/java/com/tnh/baseware/core/dtos/stats/OrganizationDailyStatsDTO.java`
   - Nested DTO structure (TaskMetrics, ProjectMetrics, PerformanceMetrics)

8. `src/main/java/com/tnh/baseware/core/mappers/stats/IOrganizationDailyStatsMapper.java`
   - MapStruct mapper for entity ↔ DTO

#### Services
9. `src/main/java/com/tnh/baseware/core/services/stats/IOrganizationStatsService.java`
   - Service interface

10. `src/main/java/com/tnh/baseware/core/services/stats/imp/OrganizationStatsService.java`
    - Business logic implementation
    - Calculation + persistence

#### Scheduled Jobs
11. `src/main/java/com/tnh/baseware/core/components/OrganizationStatsScheduler.java`
    - **Dynamic configuration-based scheduler**
    - Implements `SchedulingConfigurer` for runtime schedule registration
    - Reads schedules from `application.yml`
    - Supports multiple snapshots per day
    - Enable/disable globally or per-schedule

12. `src/main/java/com/tnh/baseware/core/configs/OrganizationStatsSchedulerConfig.java`
    - `@ConfigurationProperties` for scheduler settings
    - Flexible schedule configuration with list support
    - Validates cron expressions at startup

#### REST API
13. `src/main/java/com/tnh/baseware/core/resources/stats/OrganizationStatsResource.java`
    - 5 REST endpoints

### Files Modified

13. `src/main/resources/i18n/messages.properties`
    - Added 3 stats-related messages

14. `src/main/resources/i18n/messages_vi.properties`
    - Added 3 Vietnamese translations

### Database Changes

15. `src/main/resources/db/migration/V999__create_organization_daily_stats.sql`
    - Created `organization_daily_stats` table
    - 4 indexes (including GIN for JSONB)
    - Foreign key to `organization` table
    - Unique constraint: (organization_id, snapshot_date, snapshot_type)

**Indexes:**
- `idx_org_stats_org_date_type`: Query optimization
- `idx_org_stats_date`: Date-based queries
- `idx_org_stats_archived`: Partial index for archived data
- `idx_org_stats_extended_metrics`: GIN index for JSONB queries

---

## API Changes

### New Endpoints

All endpoints under `/api/v1/organization-stats`:

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/{orgId}/daily` | Get daily stats for specific date & type | OrgAdmin or UnitLeader |
| GET | `/{orgId}/trend` | Get stats trend over date range | OrgAdmin or UnitLeader |
| POST | `/compare` | Compare multiple organizations | UnitLeader only |
| GET | `/{orgId}/latest` | Get latest stats | OrgAdmin or UnitLeader |
| POST | `/{orgId}/recalculate` | Manual recalculation | OrgAdmin only |

**Example Request:**
```http
GET /api/v1/organization-stats/{orgId}/daily?date=2026-02-02&type=END_OF_DAY
Authorization: Bearer {token}
```

**Example Response:**
```json
{
  "id": "uuid",
  "snapshotDate": "2026-02-02",
  "snapshotType": "END_OF_DAY",
  "taskMetrics": {
    "total": 150,
    "newToday": 25,
    "completedToday": 18,
    "overdue": 10,
    "dueInNext3Days": 15,
    "inProgress": 45,
    "avgProgress": 65.5
  },
  "projectMetrics": { ... },
  "performanceMetrics": { ... }
}
```

---

## Configuration Changes

### Scheduler Configuration (Optional)

Có thể override cron expressions trong `application.yml`:

```yaml
stats:
  scheduler:
    midday-cron: "0 0 12 * * MON-FRI"   # Default: 12:00 PM
    eod-cron: "0 30 17 * * MON-FRI"      # Default: 5:30 PM
```

### Database

- **Requires:** PostgreSQL 9.4+ (for JSONB support)
- **New Table:** `organization_daily_stats`
- **Migration Version:** V999 (adjust version number before deployment)

---

## Testing

- [x] Entity persists correctly with all fields
- [x] Native queries return correct aggregations
- [x] MapStruct mapper generates correctly
- [x] Service layer calculations accurate
- [x] Scheduler cron expressions correct
- [ ] End-to-end API testing (pending deployment)
- [ ] Performance testing with large datasets
- [ ] Archive mechanism (requires 2 years of data)

---

## Deployment Notes

### Pre-Deployment Checklist

1. **Update migration version number** from V999 to actual version
2. **Run Flyway migration:** `mvn flyway:migrate`
3. **Rebuild project:** `mvn clean install` (for MapStruct)
4. **Verify scheduler timezone:** Ensure server timezone matches business hours

### Post-Deployment Verification

1. Check logs at 12:00 PM and 5:30 PM for scheduler execution
2. Verify stats are being calculated for all organizations
3. Test API endpoints with Postman/curl
4. Monitor database performance (check index usage)

### Rollback Plan

If issues occur:
1. Disable scheduler via application properties
2. Rollback migration: `mvn flyway:undo` (if configured)
3. Remove REST API endpoints from active routes

---

## Metrics Tracked

### Task Metrics (7)
1. totalTasks - Tổng task lũy kế
2. newTasksToday - Phát sinh trong ngày
3. completedToday - Hoàn thành trong ngày
4. overdueTasks - Quá hạn
5. dueInNext3Days - Sắp đến hạn
6. inProgressTasks - Đang làm
7. avgProgressRate - % hoàn thành trung bình

### Project Metrics (4)
8. totalProjects - Tổng dự án
9. activeProjects - Đang chạy
10. overdueProjects - Trễ hạn
11. completedProjectsToday - Hoàn thành hôm nay

### Performance Metrics (4)
12. completionRate - % giải quyết
13. overdueRate - % trễ hạn
14. activeUserCount - Số người làm việc
15. avgCompletionTimeHours - Thời gian xử lý TB

### Extended Metrics (Flexible)
16-17. extendedMetrics (JSONB) - Custom metrics

---

## Future Enhancements

- [ ] **Notification Service:** Auto-alert when metrics exceed thresholds
- [ ] **Export to Excel:** Download reports
- [ ] **Dashboard UI:** Visualization with charts
- [ ] **Predictive Analytics:** ML for trend prediction
- [ ] **Comparative Analysis:** Cross-department benchmarking

---

## Related Documentation

- [Implementation Plan](file:///.gemini/antigravity/brain/.../implementation_plan.md)
- [Walkthrough](file:///.gemini/antigravity/brain/.../walkthrough.md)
- [Task Breakdown](file:///.gemini/antigravity/brain/.../task.md)

---

## Related Issues/Tickets

- **Requirement:** Organization leadership dashboard statistics
- **Epic:** Executive Dashboard & Analytics
- **Story Points:** 8

---

**Total Files:** 15 (12 Java + 1 SQL + 2 i18n)  
**Total LOC:** ~1,500 lines  
**Complexity:** Medium-High
