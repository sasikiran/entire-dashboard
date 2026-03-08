package com.mzfuture.entire.checkpoint.parser;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/// Result of parsing session metadata.json (and optional prompt preview from prompt.txt).
@Data
@Builder
public class SessionParseResult {

    private String sessionId;
    private Integer sessionIndex;
    private String strategy;
    private Long sessionCreatedAt;
    private String branch;
    private Integer checkpointsCount;
    private Integer filesTouchedCount;
    private String filesTouchedJson;
    private String agent;
    private Long inputTokens;
    private Long outputTokens;
    private Integer apiCallCount;
    private Integer agentLines;
    private Integer humanAdded;
    private Integer humanModified;
    private Integer humanRemoved;
    private Integer totalCommitted;
    private BigDecimal agentPercentage;
    private String promptPreview;
}
