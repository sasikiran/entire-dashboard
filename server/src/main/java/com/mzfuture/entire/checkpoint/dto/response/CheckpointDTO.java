package com.mzfuture.entire.checkpoint.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Checkpoint response DTO")
public class CheckpointDTO {

    @Schema(description = "Primary key ID", example = "1")
    private Long id;

    @Schema(description = "12-digit hex checkpoint identifier", example = "a1b2c3d4e5f6")
    private String checkpointId;

    @Schema(description = "Repository ID", example = "1")
    private Long repoId;

    @Schema(description = "Repository name", example = "my-repo")
    private String repoName;

    @Schema(description = "Git branch name", example = "main")
    private String branch;

    @Schema(description = "Git commit SHA", example = "abc123def456")
    private String commitSha;

    @Schema(description = "Git commit message", example = "feat: add checkpoint")
    private String commitMessage;

    @Schema(description = "Git commit author display name", example = "Zhang San")
    private String commitAuthorName;

    @Schema(description = "Git commit time (Unix epoch ms)", example = "1709827200000")
    private Long commitTime;

    @Schema(description = "Number of checkpoints in this run", example = "5")
    private Integer checkpointsCount;

    @Schema(description = "Number of files touched", example = "3")
    private Integer filesTouched;

    @Schema(description = "Lines added in this commit", example = "120")
    private Integer additions;

    @Schema(description = "Lines deleted in this commit", example = "45")
    private Integer deletions;

    @Schema(description = "Cumulative token usage", example = "10000")
    private Long tokenUsage;

    @Schema(description = "AI Agent name", example = "cursor-agent")
    private String agent;

    @Schema(description = "URL to view this commit in the repository web UI", example = "https://github.com/owner/repo/commit/abc123")
    private String commitUrl;

    @Schema(description = "Creation timestamp (Unix epoch ms)", example = "1704067200000")
    private Long createdAt;

    @Schema(description = "Update timestamp (Unix epoch ms)", example = "1704067200000")
    private Long updatedAt;
}
