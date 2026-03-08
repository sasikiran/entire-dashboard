package com.mzfuture.entire.overview.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// List item for Checkpoints table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Checkpoint list item for Overview")
public class OverviewCheckpointListItemDTO {

    @Schema(description = "Primary key ID", example = "1")
    private Long id;

    @Schema(description = "12-digit hex checkpoint identifier", example = "a1b2c3d4e5f6")
    private String checkpointId;

    @Schema(description = "Git commit message", example = "feat: add checkpoint")
    private String commitMessage;

    @Schema(description = "Git commit author display name", example = "John Doe")
    private String commitAuthorName;

    @Schema(description = "Repository name", example = "my-repo")
    private String repoName;

    @Schema(description = "Git branch name", example = "main")
    private String branch;

    @Schema(description = "AI Agent name", example = "Claude Code")
    private String agent;

    @Schema(description = "Number of files touched", example = "5")
    private Integer filesTouched;

    @Schema(description = "Lines added", example = "120")
    private Integer additions;

    @Schema(description = "Lines deleted", example = "45")
    private Integer deletions;

    @Schema(description = "Git commit time (Unix epoch ms)", example = "1709827200000")
    private Long commitTime;

    @Schema(description = "Cumulative token usage", example = "10000")
    private Long tokenUsage;
}
