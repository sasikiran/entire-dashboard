package com.mzfuture.entire.gitsync.adapter;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import org.springframework.stereotype.Component;

/// Gitea 平台适配器实现（含自托管实例）
/// Gitea 与 GitHub 使用相同的 Git 认证格式和 Web commit URL 格式
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
