package com.mzfuture.entire.overview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// Chart item for Contribution scatter plot
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Checkpoint chart item for Contribution scatter plot")
public class OverviewCheckpointChartItemDTO {

    @Schema(description = "12-digit hex checkpoint identifier", example = "a1b2c3d4e5f6")
    private String checkpointId;

    @Schema(description = "Git commit time (Unix epoch ms) for chart x/y axis", example = "1709827200000")
    private Long commitTime;

    @Schema(description = "Lines added", example = "120")
    private Integer additions;

    @Schema(description = "Lines deleted", example = "45")
    private Integer deletions;

    @Schema(description = "AI Agent name", example = "Claude Code")
    private String agent;
}
