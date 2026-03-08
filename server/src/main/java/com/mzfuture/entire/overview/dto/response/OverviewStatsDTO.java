package com.mzfuture.entire.overview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/// Overview statistics response
@Data
@Schema(description = "Overview statistics response")
public class OverviewStatsDTO {

    @Schema(description = "Number of recently active projects (repos with checkpoints in range)")
    private Long activeProjectCount;

    @Schema(description = "Number of distinct submitters (commit authors)")
    private Long submitterCount;

    @Schema(description = "Total checkpoint count")
    private Long checkpointCount;

    @Schema(description = "Cumulative token usage (sum of Checkpoint.tokenUsage)")
    private Long totalTokenUsage;
}
