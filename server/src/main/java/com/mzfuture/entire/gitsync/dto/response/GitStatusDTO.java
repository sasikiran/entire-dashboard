package com.mzfuture.entire.gitsync.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/// 仓库状态信息
@Data
@Schema(description = "仓库状态信息")
public class GitStatusDTO {

    /// 仓库ID
    @Schema(description = "仓库ID")
    private Long repoId;

    /// 本地仓库是否存在
    @Schema(description = "本地仓库是否存在")
    private Boolean exists;

    /// 本地存储路径
    @Schema(description = "本地存储路径")
    private String localPath;

    /// 当前分支
    @Schema(description = "当前分支")
    private String currentBranch;

    /// 最新Commit ID
    @Schema(description = "最新Commit ID")
    private String commitId;

    /// 最新Commit消息
    @Schema(description = "最新Commit消息")
    private String commitMessage;

    /// 最后提交时间
    @Schema(description = "最后提交时间")
    private Long lastCommitTime;
}
