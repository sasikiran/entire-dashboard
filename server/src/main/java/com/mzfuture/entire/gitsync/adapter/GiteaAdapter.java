package com.mzfuture.entire.gitsync.adapter;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import org.springframework.stereotype.Component;

/// Gitea platform adapter implementation (including self-hosted instances)
/// Gitea uses the same Git authentication format and Web commit URL format as GitHub
@Component
public class GiteaAdapter implements GitPlatformAdapter {

    @Override
    public RepositoryPlatform getPlatform() {
        return RepositoryPlatform.GITEA;
    }

    @Override
    public String buildAuthUrl(String webUrl, String accessToken) {
        return GitUrlUtils.buildTokenInUserInfoUrl(webUrl, accessToken);
    }

    @Override
    public String buildCommitUrl(String webUrl, String commitSha) {
        if (webUrl == null || webUrl.isBlank() || commitSha == null || commitSha.isBlank()) {
            return null;
        }
        String base = GitUrlUtils.normalizeWebUrlForCommit(webUrl);
        return base != null ? base + "/commit/" + commitSha : null;
    }
}
