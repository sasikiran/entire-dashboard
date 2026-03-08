package com.mzfuture.entire.checkpoint.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Checkpoint update request parameters")
public class CheckpointUpdateParams {

    @NotNull(message = "Checkpoint ID cannot be null")
    @Schema(description = "Checkpoint primary key ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Size(min = 12, max = 12, message = "Checkpoint ID must be 12 hex characters")
    @Schema(description = "12-digit hex checkpoint identifier", example = "a1b2c3d4e5f6")
    private String checkpointId;

    @Schema(description = "Repository ID", example = "1")
    private Long repoId;

    @Schema(description = "Git branch name", example = "main")
    private String branch;

    @Schema(description = "Git commit SHA", example = "abc123def456")
    private String commitSha;

    @Schema(description = "Git commit message", example = "feat: add checkpoint")
    private String commitMessage;

    @Schema(description = "Number of checkpoints in this run", example = "5")
    private Integer checkpointsCount;

    @Schema(description = "Number of files touched", example = "3")
    private Integer filesTouched;

    @Schema(description = "Cumulative token usage", example = "10000")
    private Long tokenUsage;

    @Schema(description = "AI Agent name", example = "cursor-agent")
    private String agent;
}
