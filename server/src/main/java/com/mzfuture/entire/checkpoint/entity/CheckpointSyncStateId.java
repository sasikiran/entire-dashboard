package com.mzfuture.entire.checkpoint.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/// Composite primary key: (repo_id, branch), used to record checkpoint incremental sync state by branch
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CheckpointSyncStateId implements Serializable {

    private static final long serialVersionUID = 1L;

    /// Repository ID (FK to repository.id)
    private Long repoId;

    /// Branch name (e.g. main, entire/checkpoints/v1, etc.)
    private String branch;
}
