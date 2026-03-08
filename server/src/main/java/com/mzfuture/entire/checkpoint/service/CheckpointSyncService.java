package com.mzfuture.entire.checkpoint.service;

/// Orchestrates checkpoint sync: branch check, clone/pull, commit walk, parse, upsert.
public interface CheckpointSyncService {

    /// Sync one repo: clone/pull entire/checkpoints/v1, walk commits, upsert checkpoints.
    /// Skips silently if remote has no entire/checkpoints/v1 branch.
    /// @param repoId repository id
    /// @param fullScan if true, ignore last_processed and walk all commits from HEAD to root
    void syncRepo(Long repoId, boolean fullScan);

    /// Sync all repos (same as syncRepo for each; repos without branch are skipped).
    /// @param fullScan if true, full scan for each repo
    void syncAllRepos(boolean fullScan);
}
