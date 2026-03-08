package com.mzfuture.entire.overview.dto.response;

import com.mzfuture.entire.common.dto.PagerPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/// Overview checkpoints response: chart data, paginated list, agent stats
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Overview checkpoints response")
public class OverviewCheckpointsResponseDTO {

    @Schema(description = "Chart data for Contribution scatter plot (max 500 items)")
    private List<OverviewCheckpointChartItemDTO> chartData;

    @Schema(description = "Whether chart data was truncated (total > 500)")
    private Boolean chartDataTruncated;

    @Schema(description = "Paginated checkpoint list")
    private PagerPayload<OverviewCheckpointListItemDTO> list;

    @Schema(description = "Agent contribution statistics for legend")
    private List<OverviewAgentStatDTO> agentStats;
}
