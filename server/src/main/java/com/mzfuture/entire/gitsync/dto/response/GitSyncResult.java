package com.mzfuture.entire.gitsync.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/// Git同步结果
@Data
@Schema(description = "Git同步结果")
public class GitSyncResult {

    /// 同步状态: SUCCESS/FAILED
    @Schema(description = "同步状态: SUCCESS/FAILED")
    private String status;

    /// 仓库ID
    @Schema(description = "仓库ID")
    private Long repoId;

    /// 仓库名称
    @Schema(description = "仓库名称")
    private String repoName;

    /// 存储路径
    @Schema(description = "存储路径")
    private String localPath;

    /// 当前分支
    @Schema(description = "当前分支")
    private String branch;

    /// 最新Commit ID
    @Schema(description = "最新Commit ID")
    private String commitId;

    /// 最新Commit消息
    @Schema(description = "最新Commit消息")
    private String commitMessage;

    /// 同步时间戳
    @Schema(description = "同步时间戳")
    private Long syncTime;

    /// 是否有更新（仅pull时有效）
    @Schema(description = "是否有更新")
    private Boolean hasUpdate;

    /// 错误信息，失败时返回
    @Schema(description = "错误信息，失败时返回")
    private String errorMessage;
}
