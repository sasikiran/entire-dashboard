package com.mzfuture.entire.gitsync.dto.response;

import lombok.Builder;
import lombok.Data;

/// Pull操作结果
@Data
@Builder
public class PullResult {

    /// 是否有更新
    private boolean updated;

    /// Commit ID
    private String commitId;

    /// Commit消息
    private String commitMessage;

    /// Commit作者
    private String author;

    /// Commit时间
    private Long commitTime;
}
