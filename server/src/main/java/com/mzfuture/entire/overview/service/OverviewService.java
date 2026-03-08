package com.mzfuture.entire.overview.service;

import com.mzfuture.entire.common.dto.PagerPayload;
import com.mzfuture.entire.overview.dto.request.OverviewCheckpointsParams;
import com.mzfuture.entire.overview.dto.request.OverviewStatsParams;
import com.mzfuture.entire.overview.dto.response.OverviewCheckpointListItemDTO;
import com.mzfuture.entire.overview.dto.response.OverviewCheckpointsResponseDTO;
import com.mzfuture.entire.overview.dto.response.OverviewStatsDTO;

/// Overview statistics service
public interface OverviewService {

    /// Get overview statistics for the given time range
    OverviewStatsDTO getStats(OverviewStatsParams params);

    /// Get checkpoints for Contribution chart and list (time range, chart limit 500, list paginated)
    OverviewCheckpointsResponseDTO getCheckpoints(OverviewCheckpointsParams params);

    /// Get paginated checkpoint list only (for pagination, no chart data)
    PagerPayload<OverviewCheckpointListItemDTO> getCheckpointsList(OverviewCheckpointsParams params);
}
