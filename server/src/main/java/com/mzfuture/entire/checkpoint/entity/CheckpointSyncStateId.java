package com.mzfuture.entire.checkpoint.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/// 复合主键：(repo_id, branch)，用于按分支记录 checkpoint 增量同步状态
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CheckpointSyncStateId implements Serializable {

    private static final long serialVersionUID = 1L;

    /// 仓库 ID（FK to repository.id）
    private Long repoId;

    /// 分支名（如 main、entire/checkpoints/v1 等）
    private String branch;
}
