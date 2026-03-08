package com.mzfuture.entire.checkpoint.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Checkpoint search request parameters")
public class CheckpointSearchParams {

    @Schema(description = "Repository IDs (multi-select)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<Long> repoIds;

    @Schema(description = "Commit author names (multi-select, exact match)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> commitAuthorNames;

    @Schema(description = "Branch name (fuzzy)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "main")
    private String branch;

    @Schema(description = "Commit message (fuzzy)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "feat")
    private String commitMessage;

    @Schema(description = "Start time (Unix epoch milliseconds)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1709251200000")
    private Long startTime;

    @Schema(description = "End time (Unix epoch milliseconds)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1711929600000")
    private Long endTime;
}
