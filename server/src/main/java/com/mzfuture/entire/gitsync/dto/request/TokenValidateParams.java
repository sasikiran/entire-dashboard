package com.mzfuture.entire.gitsync.dto.request;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/// Token 校验请求参数
@Data
public class TokenValidateParams {

    @NotBlank(message = "Web URL cannot be blank")
    @Schema(description = "Repository web URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://github.com/owner/repo")
    private String webUrl;

    @NotNull(message = "Platform cannot be null")
    @Schema(description = "Git platform", requiredMode = Schema.RequiredMode.REQUIRED, example = "GITHUB")
    private RepositoryPlatform platform;

    @NotBlank(message = "Access token cannot be blank")
    @Schema(description = "Access token to validate", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessToken;
}
