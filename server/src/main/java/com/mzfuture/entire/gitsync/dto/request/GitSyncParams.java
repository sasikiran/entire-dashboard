package com.mzfuture.entire.gitsync.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/// Git仓库同步请求参数
@Data
@Schema(description = "Git仓库同步请求参数")
public class GitSyncParams {

    /// 仓库WebURL
    @NotBlank(message = "仓库WebURL不能为空")
    @Schema(description = "仓库WebURL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String webUrl;

    /// 访问令牌，为空则使用仓库配置的token
    @Schema(description = "访问令牌，为空则使用仓库配置的token")
    private String accessToken;

    /// 指定分支，默认entire/checkpoints/v1
    @Schema(description = "指定分支，默认entire/checkpoints/v1")
    private String branch;
}
