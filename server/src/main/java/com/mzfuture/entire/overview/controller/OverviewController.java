package com.mzfuture.entire.overview.controller;

import com.mzfuture.entire.overview.dto.request.OverviewCheckpointsParams;
import com.mzfuture.entire.overview.dto.request.OverviewStatsParams;
import com.mzfuture.entire.overview.dto.response.OverviewCheckpointsResponseDTO;
import com.mzfuture.entire.overview.dto.response.OverviewStatsDTO;
import com.mzfuture.entire.overview.service.OverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// Overview API: dashboard statistics
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/overview")
@Tag(name = "Overview", description = "Dashboard overview statistics")
public class OverviewController {

    private final OverviewService overviewService;

    /// Get overview statistics for the given time range
    @GetMapping("/stats")
    @Operation(summary = "Get overview statistics", description = "Returns active projects, submitters, checkpoints, and token usage for the given time range. Default: last 7 days.")
    public OverviewStatsDTO stats(@ModelAttribute @Valid OverviewStatsParams params) {
        return overviewService.getStats(params);
    }

    /// Get checkpoints for Contribution chart and list
    @GetMapping("/checkpoints")
    @Operation(summary = "Get overview checkpoints", description = "Returns chart data (max 500), paginated list, and agent stats for the given time range.")
    public OverviewCheckpointsResponseDTO checkpoints(@ModelAttribute @Valid OverviewCheckpointsParams params) {
        return overviewService.getCheckpoints(params);
    }

    /// Get paginated checkpoint list only (for pagination, no chart data)
    @GetMapping("/checkpoints/list")
    @Operation(summary = "Get checkpoint list", description = "Returns paginated checkpoint list only. Use for pagination to avoid refetching chart data.")
    public com.mzfuture.entire.common.dto.PagerPayload<com.mzfuture.entire.overview.dto.response.OverviewCheckpointListItemDTO> checkpointsList(
            @ModelAttribute @Valid OverviewCheckpointsParams params) {
        return overviewService.getCheckpointsList(params);
    }
}
