package com.mzfuture.entire.gitsync.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/// Repository status information
@Data
@Schema(description = "Repository status information")
public class GitStatusDTO {

    /// Repository ID
    @Schema(description = "Repository ID")
    private Long repoId;

    /// Whether local repository exists
    @Schema(description = "Whether local repository exists")
    private Boolean exists;

    /// Local storage path
    @Schema(description = "Local storage path")
    private String localPath;

    /// Current branch
    @Schema(description = "Current branch")
    private String currentBranch;

    /// Latest Commit ID
    @Schema(description = "Latest Commit ID")
    private String commitId;

    /// Latest Commit message
    @Schema(description = "Latest Commit message")
    private String commitMessage;

    /// Last commit time
    @Schema(description = "Last commit time")
    private Long lastCommitTime;
}
