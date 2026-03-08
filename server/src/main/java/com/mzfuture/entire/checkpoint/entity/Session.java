package com.mzfuture.entire.checkpoint.entity;

import com.mzfuture.entire.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/// Session entity for a single agent run within a checkpoint (one checkpoint has many sessions).
/// tasks/ sub-agent checkpoints are excluded; see design for future extension.
@Getter
@Setter
@Entity
public class Session extends BaseEntity {

    /// Reference to checkpoint (Checkpoint.id)
    private Long checkpointId;

    /// Session identifier for display reference (e.g. ses_36fff781effeQzIdUBtzUiXS4q)
    private String sessionId;

    /// 0-based index within the checkpoint
    private Integer sessionIndex;

    /// Strategy (e.g. manual-commit)
    private String strategy;

    /// Session creation time from metadata (Unix epoch milliseconds)
    private Long sessionCreatedAt;

    /// Git branch name
    private String branch;

    /// Number of checkpoints in this run
    private Integer checkpointsCount;

    /// Number of files touched (length of files_touched array)
    private Integer filesTouchedCount;

    /// JSON array of touched file paths for frontend display
    private String filesTouchedJson;

    /// AI Agent name
    private String agent;

    /// Token usage: input
    private Long inputTokens;

    /// Token usage: output
    private Long outputTokens;

    /// API call count
    private Integer apiCallCount;

    /// Attribution: lines written by agent
    private Integer agentLines;

    /// Attribution: lines added by human
    private Integer humanAdded;

    /// Attribution: lines modified by human
    private Integer humanModified;

    /// Attribution: lines removed by human
    private Integer humanRemoved;

    /// Attribution: total committed lines
    private Integer totalCommitted;

    /// Attribution: agent percentage
    private BigDecimal agentPercentage;

    /// First line of prompt.txt for list preview
    private String promptPreview;
}
