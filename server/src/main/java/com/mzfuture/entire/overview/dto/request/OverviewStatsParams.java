package com.mzfuture.entire.overview.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/// Overview statistics query parameters
@Data
@Schema(description = "Overview statistics query parameters")
public class OverviewStatsParams {

    @Schema(description = "Start time (Unix ms). Default: 7 days ago at 00:00:00", example = "1709827200000")
    private Long startTime;

    @Schema(description = "End time (Unix ms). Default: today at 23:59:59", example = "1710431999999")
    private Long endTime;
}
