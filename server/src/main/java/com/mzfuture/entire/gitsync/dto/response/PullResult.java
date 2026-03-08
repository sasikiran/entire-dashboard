package com.mzfuture.entire.gitsync.dto.response;

import lombok.Builder;
import lombok.Data;

/// Pull operation result
@Data
@Builder
public class PullResult {

    /// Whether there is an update
    private boolean updated;

    /// Commit ID
    private String commitId;

    /// Commit message
    private String commitMessage;

    /// Commit author
    private String author;

    /// Commit time
    private Long commitTime;
}
