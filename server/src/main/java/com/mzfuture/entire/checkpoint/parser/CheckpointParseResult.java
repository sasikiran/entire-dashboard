package com.mzfuture.entire.checkpoint.parser;

import lombok.Builder;
import lombok.Data;

/// Result of parsing checkpoint metadata.json (and optional last session agent).
@Data
@Builder
public class CheckpointParseResult {

    private String checkpointId;
    private String branch;
    private Integer checkpointsCount;
    private Integer filesTouched;
    private Long tokenUsage;
    private String agent;  // from last session metadata, null if not parsed
}
