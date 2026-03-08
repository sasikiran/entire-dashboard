package com.mzfuture.entire.checkpoint.repository;

import com.mzfuture.entire.checkpoint.entity.CheckpointSyncState;
import com.mzfuture.entire.checkpoint.entity.CheckpointSyncStateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/// 按 (repo_id, branch) 查询 checkpoint 增量同步状态
public interface CheckpointSyncStateRepository extends JpaRepository<CheckpointSyncState, CheckpointSyncStateId> {

    Optional<CheckpointSyncState> findByRepoIdAndBranch(Long repoId, String branch);

    List<CheckpointSyncState> findByRepoId(Long repoId);
}
