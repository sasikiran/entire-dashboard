package com.mzfuture.entire.checkpoint.repository;

import com.mzfuture.entire.checkpoint.entity.CheckpointSyncState;
import com.mzfuture.entire.checkpoint.entity.CheckpointSyncStateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/// Query checkpoint incremental sync state by (repo_id, branch)
public interface CheckpointSyncStateRepository extends JpaRepository<CheckpointSyncState, CheckpointSyncStateId> {

    Optional<CheckpointSyncState> findByRepoIdAndBranch(Long repoId, String branch);

    List<CheckpointSyncState> findByRepoId(Long repoId);
}
