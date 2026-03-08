package com.mzfuture.entire.checkpoint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/// Record incremental sync state by (repo_id, branch); only content branches participate in walk, each branch has independent lastProcessedCommitSha.
@Getter
@Setter
@Entity
@Table(name = "checkpoint_sync_state")
@IdClass(CheckpointSyncStateId.class)
public class CheckpointSyncState {

    /// Repository ID (PK, FK to repository.id)
    @Id
    private Long repoId;

    /// Branch name (PK), e.g., main
    @Id
    private String branch;

    /// Latest processed commit SHA on this branch; null means this branch has never been synced
    private String lastProcessedCommitSha;

    /// Last successful sync completion time for this branch (Unix milliseconds)
    private Long lastSyncAt;

    /// Error message from last sync failure
    @Column(length = 512)
    private String lastError;

    /// Creation timestamp (Unix milliseconds)
    private Long createdAt;

    /// Update timestamp (Unix milliseconds)
    private Long updatedAt;
}
