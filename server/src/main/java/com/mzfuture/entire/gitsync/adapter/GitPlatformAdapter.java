package com.mzfuture.entire.gitsync.adapter;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;

/// Git platform adapter interface
/// Supports URL building and authentication methods for different Git platforms
public interface GitPlatformAdapter {

    /// Get the supported platform type
    ///
    /// @return platform type enum
    RepositoryPlatform getPlatform();

    /// Build Git URL with authentication
    ///
    /// @param webUrl original Web URL
    /// @param accessToken access token
    /// @return URL with authentication
    String buildAuthUrl(String webUrl, String accessToken);

    /// Build single commit view URL for repository Web interface
    ///
    /// @param webUrl repository Web URL (supports .git suffix, will be normalized first)
    /// @param commitSha commit SHA
    /// @return clickable commit detail page URL, returns null if parameters are invalid
    String buildCommitUrl(String webUrl, String commitSha);

    /// Validate token validity (optional implementation)
    ///
    /// @param webUrl repository URL
    /// @param accessToken access token
    /// @return whether token is valid
    default boolean validateToken(String webUrl, String accessToken) {
        return true;
    }
}
