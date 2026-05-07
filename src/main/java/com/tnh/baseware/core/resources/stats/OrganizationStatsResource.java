package com.tnh.baseware.core.resources.stats;

import com.tnh.baseware.core.dtos.stats.OrganizationDailyStatsDTO;
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
     * Get daily stats for a specific organization and date
     * 
     * GET /api/v1/organization-stats/{orgId}/daily?date=2026-02-02
     */
    @GetMapping("/{orgId}/daily")
    @PreAuthorize("@securityUtils.canAccessOrganizationStats(#orgId)")
    public ResponseEntity<OrganizationDailyStatsDTO> getDailyStats(
            @PathVariable UUID orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        OrganizationDailyStatsDTO stats = statsService.getStats(orgId, toInstant(date));
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

        List<OrganizationDailyStatsDTO> trend = statsService.getStatsTrend(orgId, toInstant(from), toInstant(to));
        return ResponseEntity.ok(trend);
    }

    /**
     * Compare multiple organizations on a specific date
     * User must have access to ALL requested organizations
     * 
     * POST /api/v1/organization-stats/compare
     * Body: { "orgIds": [...], "date": "2026-02-02" }
     */
    @PostMapping("/compare")
    public ResponseEntity<List<OrganizationDailyStatsDTO>> compareOrganizations(
            @RequestBody CompareRequest request) {

        List<OrganizationDailyStatsDTO> comparison = statsService.compareOrganizations(
                request.orgIds(),
                toInstant(request.date()));
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
     * /api/v1/organization-stats/{orgId}/recalculate?date=2026-02-02
     */
    @PostMapping("/{orgId}/recalculate")
    @PreAuthorize("@securityUtils.canAccessOrganizationStats(#orgId)")
    public ResponseEntity<Map<String, Object>> recalculateStats(
            @PathVariable UUID orgId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Trigger async recalculation
        statsService.triggerRecalculationAsync(orgId, toInstant(date));

        return ResponseEntity.accepted().body(Map.of(
                "message", "Calculation started in background. Please check back later.",
                "status", "ACCEPTED",
                "orgId", orgId,
                "date", date));
    }

    private java.time.Instant toInstant(LocalDate date) {
        return date.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant();
    }

    // DTO for compare request
    record CompareRequest(
            List<UUID> orgIds,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    }
}
