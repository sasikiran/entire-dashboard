package com.mzfuture.entire.checkpoint.entity;

import com.mzfuture.entire.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/// Checkpoint entity for git commit snapshot and agent run metadata
@Getter
@Setter
@Entity
public class Checkpoint extends BaseEntity {

    /// 12-digit hex checkpoint identifier
    private String checkpointId;

    /// Repository ID
    private Long repoId;

    /// Repository name (redundant for listing)
    private String repoName;

    /// Git branch name
    private String branch;

    /// Git commit SHA
    private String commitSha;

    /// Git commit message
    private String commitMessage;

    /// Git commit author display name
    private String commitAuthorName;

    /// Git commit time (Unix epoch milliseconds), from RevCommit.getCommitTime()
    private Long commitTime;

    /// Number of checkpoints in this run
    private Integer checkpointsCount;

    /// Number of files touched
    private Integer filesTouched;

    /// Lines added in this commit (from git diff)
    private Integer additions;

    /// Lines deleted in this commit (from git diff)
    private Integer deletions;

    /// Cumulative token usage
    private Long tokenUsage;

    /// AI Agent name
    private String agent;
}
