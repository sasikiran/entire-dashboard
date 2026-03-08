package com.mzfuture.entire.overview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// Agent contribution statistics for chart legend
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent contribution statistics")
public class OverviewAgentStatDTO {

    @Schema(description = "AI Agent name", example = "Claude Code")
    private String agent;

    @Schema(description = "Checkpoint count for this agent", example = "3")
    private long count;

    @Schema(description = "Percentage of total checkpoints", example = "31")
    private int percentage;
}
