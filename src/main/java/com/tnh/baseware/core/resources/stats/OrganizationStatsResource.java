package com.tnh.baseware.core.resources.stats;

import com.tnh.baseware.core.dtos.stats.OrganizationDailyStatsDTO;
import com.tnh.baseware.core.enums.stats.SnapshotType;
import com.tnh.baseware.core.services.stats.IOrganizationStatsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for Organization Daily Statistics
 * 
 * Endpoints for retrieving daily stats snapshots, trends, and comparisons
 */
@RestController
@RequestMapping("/api/v1/organization-stats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationStatsResource {

    IOrganizationStatsService statsService;

    /**
     * Get daily stats for a specific organization, date, and snapshot type
     * 
     * GET /api/v1/organization-stats/{orgId}/daily?date=2026-02-02&type=END_OF_DAY
     */
    @GetMapping("/{orgId}/daily")
    @PreAuthorize("@securityUtils.canAccessOrganizationStats(#orgId)")
    public ResponseEntity<OrganizationDailyStatsDTO> getDailyStats(
            @PathVariable UUID orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam SnapshotType type) {

        OrganizationDailyStatsDTO stats = statsService.getStats(orgId, date, type);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get stats trend over a date range
     * 
     * GET /api/v1/organization-stats/{orgId}/trend?from=2026-01-01&to=2026-02-02
     */
    @GetMapping("/{orgId}/trend")
    @PreAuthorize("@securityUtils.canAccessOrganizationStats(#orgId)")
    public ResponseEntity<List<OrganizationDailyStatsDTO>> getStatsTrend(
            @PathVariable UUID orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<OrganizationDailyStatsDTO> trend = statsService.getStatsTrend(orgId, from, to);
        return ResponseEntity.ok(trend);
    }

    /**
     * Compare multiple organizations on a specific date
     * User must have access to ALL requested organizations
     * 
     * POST /api/v1/organization-stats/compare
     * Body: { "orgIds": [...], "date": "2026-02-02", "type": "END_OF_DAY" }
     */
    @PostMapping("/compare")
    public ResponseEntity<List<OrganizationDailyStatsDTO>> compareOrganizations(
            @RequestBody CompareRequest request) {

        List<OrganizationDailyStatsDTO> comparison = statsService.compareOrganizations(
                request.orgIds(),
                request.date(),
                request.type());
        return ResponseEntity.ok(comparison);
    }

    /**
     * Get latest stats for an organization
     * 
     * GET /api/v1/organization-stats/{orgId}/latest
     */
    @GetMapping("/{orgId}/latest")
    @PreAuthorize("@securityUtils.canAccessOrganizationStats(#orgId)")
    public ResponseEntity<OrganizationDailyStatsDTO> getLatestStats(@PathVariable UUID orgId) {
        OrganizationDailyStatsDTO stats = statsService.getLatestStats(orgId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Recalculate stats for a specific date (manual trigger)
     * Useful for backfill or fixing incorrect data
     * 
     * POST
     * /api/v1/organization-stats/{orgId}/recalculate?date=2026-02-02&type=END_OF_DAY
     */
    @PostMapping("/{orgId}/recalculate")
    @PreAuthorize("@securityUtils.canAccessOrganizationStats(#orgId)")
    public ResponseEntity<Map<String, Object>> recalculateStats(
            @PathVariable UUID orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam SnapshotType type) {

        // Trigger async recalculation
        statsService.triggerRecalculationAsync(orgId, date, type);

        return ResponseEntity.accepted().body(Map.of(
                "message", "Calculation started in background. Please check back later.",
                "status", "ACCEPTED",
                "orgId", orgId,
                "date", date,
                "type", type));
    }

    // DTO for compare request
    record CompareRequest(
            List<UUID> orgIds,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            SnapshotType type) {
    }
}
