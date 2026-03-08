package com.mzfuture.entire.gitsync.service;

import com.mzfuture.entire.gitsync.dto.response.GitStatusDTO;
import com.mzfuture.entire.gitsync.dto.response.PullResult;

/// Git operation service interface
/// Provides basic Git operation functions (clone, pull, status, etc.)
public interface GitOperationService {

    /// Clone repository (single branch shallow clone)
    ///
    /// @param repoId Repository ID
    /// @param authUrl URL with authentication
    /// @param branch Branch name
    /// @return Local repository path
    String cloneRepository(Long repoId, String authUrl, String branch);

    /// Clone repository (depth can be specified, depth<=0 means use global default depth)
    ///
    /// @param repoId Repository ID
    /// @param authUrl URL with authentication
    /// @param branch Branch name
    /// @param depth Clone depth, 0=full, >0=shallow clone depth
    /// @return Local repository path
    String cloneRepository(Long repoId, String authUrl, String branch, int depth);

    /// Pull latest code
    ///
    /// @param repoId Repository ID
    /// @param authUrl URL with authentication
    /// @param branch Branch name
    /// @return Pull operation result
    PullResult pullRepository(Long repoId, String authUrl, String branch);

    /// Get repository current status
    ///
    /// @param repoId Repository ID
    /// @return Repository status information
    GitStatusDTO getRepositoryStatus(Long repoId);

    /// Check if local repository exists
    ///
    /// @param repoId Repository ID
    /// @return Whether exists
    boolean isRepositoryExists(Long repoId);

    /// Get local repository path
    ///
    /// @param repoId Repository ID
    /// @return Local path
    String getLocalRepositoryPath(Long repoId);

    /// Delete local repository
    ///
    /// @param repoId Repository ID
    /// @return Whether successfully deleted
    boolean deleteRepository(Long repoId);

    /// Get all branch names from remote repository
    ///
    /// @param authUrl URL with authentication
    /// @return List of branch names
    java.util.List<String> listRemoteBranches(String authUrl);

    /// Fetch specified branches on existing local repository (without checkout switch), used for checkpoint multi-branch sync
    ///
    /// @param repoId Repository ID
    /// @param authUrl URL with authentication
    /// @param branchNames List of branch names to fetch
    void fetchBranches(Long repoId, String authUrl, java.util.List<String> branchNames);
}
