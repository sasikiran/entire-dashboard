package com.mzfuture.entire.checkpoint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/// 按 (repo_id, branch) 记录增量同步状态；仅内容分支参与 walk，每条分支独立 lastProcessedCommitSha。
@Getter
@Setter
@Entity
@Table(name = "checkpoint_sync_state")
@IdClass(CheckpointSyncStateId.class)
public class CheckpointSyncState {

    /// 仓库 ID（PK，FK to repository.id）
    @Id
    private Long repoId;

    /// 分支名（PK），如 main
    @Id
    private String branch;

    /// 该分支上已处理到的最新 commit SHA；null 表示该分支尚未同步过
    private String lastProcessedCommitSha;

    /// 该分支最后一次成功同步完成时间（Unix 毫秒）
    private Long lastSyncAt;

    /// 最后一次同步失败时的错误信息
    @Column(length = 512)
    private String lastError;

    /// 创建时间（Unix 毫秒）
    private Long createdAt;

    /// 更新时间（Unix 毫秒）
    private Long updatedAt;
}
