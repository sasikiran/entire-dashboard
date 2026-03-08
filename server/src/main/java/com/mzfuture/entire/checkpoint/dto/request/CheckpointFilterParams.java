package com.mzfuture.entire.checkpoint.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Checkpoint filter params for repos and commit-authors dropdown")
public class CheckpointFilterParams {

    @NotNull(message = "startTime is required")
    @Schema(description = "Start time (Unix epoch milliseconds)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long startTime;

    @NotNull(message = "endTime is required")
    @Schema(description = "End time (Unix epoch milliseconds)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long endTime;

    @Schema(description = "Repository IDs (when filtering commit-authors)")
    private List<Long> repoIds;

    @Schema(description = "Commit author names (when filtering repos)")
    private List<String> commitAuthorNames;

    @Schema(description = "Commit message (fuzzy)")
    private String commitMessage;
}
