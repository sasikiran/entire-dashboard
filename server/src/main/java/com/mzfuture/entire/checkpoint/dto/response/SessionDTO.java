package com.mzfuture.entire.checkpoint.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Session response DTO")
public class SessionDTO {

    @Schema(description = "Primary key ID", example = "1")
    private Long id;

    @Schema(description = "Checkpoint ID (FK)", example = "1")
    private Long checkpointId;

    @Schema(description = "Session identifier for reference", example = "ses_36fff781effeQzIdUBtzUiXS4q")
    private String sessionId;

    @Schema(description = "0-based index within checkpoint", example = "0")
    private Integer sessionIndex;

    @Schema(description = "Strategy", example = "manual-commit")
    private String strategy;

    @Schema(description = "Session creation time (Unix epoch ms)", example = "1704067200000")
    private Long sessionCreatedAt;

    @Schema(description = "Git branch name", example = "main")
    private String branch;

    @Schema(description = "Number of checkpoints in run", example = "2")
    private Integer checkpointsCount;

    @Schema(description = "Count of files touched", example = "1")
    private Integer filesTouchedCount;

    @Schema(description = "JSON array of touched file paths", example = "[\"README.md\"]")
    private String filesTouchedJson;

    @Schema(description = "AI Agent name", example = "OpenCode")
    private String agent;

    @Schema(description = "Input tokens", example = "721")
    private Long inputTokens;

    @Schema(description = "Output tokens", example = "426")
    private Long outputTokens;

    @Schema(description = "API call count", example = "6")
    private Integer apiCallCount;

    @Schema(description = "Attribution: agent lines", example = "30")
    private Integer agentLines;

    @Schema(description = "Attribution: human added", example = "35")
    private Integer humanAdded;

    @Schema(description = "Attribution: human modified", example = "0")
    private Integer humanModified;

    @Schema(description = "Attribution: human removed", example = "0")
    private Integer humanRemoved;

    @Schema(description = "Attribution: total committed", example = "65")
    private Integer totalCommitted;

    @Schema(description = "Attribution: agent percentage", example = "46.15")
    private BigDecimal agentPercentage;

    @Schema(description = "Prompt preview for list (first line of prompt.txt)", example = "提供中英文两个版本的README")
    private String promptPreview;

    @Schema(description = "Creation timestamp (Unix epoch ms)", example = "1704067200000")
    private Long createdAt;

    @Schema(description = "Update timestamp (Unix epoch ms)", example = "1704067200000")
    private Long updatedAt;
}
