package com.mzfuture.entire.gitsync.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/// Git repository sync request parameters
@Data
@Schema(description = "Git repository sync request parameters")
public class GitSyncParams {

    /// Repository Web URL
    @NotBlank(message = "Repository Web URL cannot be empty")
    @Schema(description = "Repository Web URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String webUrl;

    /// Access token, uses repository configured token if empty
    @Schema(description = "Access token, uses repository configured token if empty")
    private String accessToken;

    /// Specified branch, default entire/checkpoints/v1
    @Schema(description = "Specified branch, default entire/checkpoints/v1")
    private String branch;
}
