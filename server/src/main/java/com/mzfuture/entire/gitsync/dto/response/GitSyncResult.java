package com.mzfuture.entire.gitsync.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/// Git sync result
@Data
@Schema(description = "Git sync result")
public class GitSyncResult {

    /// Sync status: SUCCESS/FAILED
    @Schema(description = "Sync status: SUCCESS/FAILED")
    private String status;

    /// Repository ID
    @Schema(description = "Repository ID")
    private Long repoId;

    /// Repository name
    @Schema(description = "Repository name")
    private String repoName;

    /// Storage path
    @Schema(description = "Storage path")
    private String localPath;

    /// Current branch
    @Schema(description = "Current branch")
    private String branch;

    /// Latest Commit ID
    @Schema(description = "Latest Commit ID")
    private String commitId;

    /// Latest Commit message
    @Schema(description = "Latest Commit message")
    private String commitMessage;

    /// Sync timestamp
    @Schema(description = "Sync timestamp")
    private Long syncTime;

    /// Whether there is an update (only valid for pull)
    @Schema(description = "Whether there is an update")
    private Boolean hasUpdate;

    /// Error message, returned on failure
    @Schema(description = "Error message, returned on failure")
    private String errorMessage;
}
