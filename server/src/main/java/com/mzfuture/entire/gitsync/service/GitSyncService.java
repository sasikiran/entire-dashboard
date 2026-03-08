package com.mzfuture.entire.gitsync.service;

import com.mzfuture.entire.gitsync.dto.request.GitSyncParams;
import com.mzfuture.entire.gitsync.dto.request.TokenValidateParams;
import com.mzfuture.entire.gitsync.dto.response.GitBranchesDTO;
import com.mzfuture.entire.gitsync.dto.response.GitSyncResult;
import com.mzfuture.entire.gitsync.dto.response.TokenValidateResult;

/// Git sync service interface
/// Provides high-level sync functionality including retry, error handling, etc.
public interface GitSyncService {

    /// Sync repository code
    ///
    /// @param params sync parameters
    /// @return sync result
    GitSyncResult syncRepository(GitSyncParams params);

    /// Sync by Repo ID (using configuration stored in database)
    ///
    /// @param repoId repository ID
    /// @return sync result
    GitSyncResult syncRepositoryById(Long repoId);

    /// Sync by Repo ID with specified branch and clone depth (used for checkpoint sync, etc.)
    ///
    /// @param repoId repository ID
    /// @param branch branch name, null uses default
    /// @param cloneDepth clone depth, 0=full, >0=shallow clone; negative uses global default
    /// @return sync result
    GitSyncResult syncRepositoryById(Long repoId, String branch, int cloneDepth);

    /// Get all branches of repository
    ///
    /// @param repoId repository ID
    /// @return branch list information
    GitBranchesDTO getBranches(Long repoId);

    /// Pull all branches for checkpoint sync: if no local repo, first clone metadata branch then fetch all branches; if repo exists, fetch all branches (not dependent on current HEAD)
    ///
    /// @param repoId repository ID
    /// @param metadataBranch branch name that only stores checkpoint metadata (e.g., entire/checkpoints/v1), prioritized during clone
    /// @param cloneDepth clone depth, 0=full, >0=shallow clone; negative uses global default
    /// @return sync result
    GitSyncResult syncRepositoryAllBranchesForCheckpoint(Long repoId, String metadataBranch, int cloneDepth);

    /// Fetch metadata branch to latest (only fetch this branch), called before walking content branches to parse checkpoint, ensures reading latest metadata file
    ///
    /// @param repoId repository ID
    /// @param metadataBranch metadata branch name (e.g., entire/checkpoints/v1)
    void fetchMetadataBranch(Long repoId, String metadataBranch);

    /// Validate access token validity (calls platform API)
    ///
    /// @param params webUrl, platform, accessToken
    /// @return validation result
    TokenValidateResult validateToken(TokenValidateParams params);
}
