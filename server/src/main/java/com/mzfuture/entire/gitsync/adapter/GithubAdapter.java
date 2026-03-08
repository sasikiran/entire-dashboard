package com.mzfuture.entire.gitsync.adapter;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import com.mzfuture.entire.common.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/// GitHub 平台适配器实现
@Slf4j
@Component
public class GithubAdapter implements GitPlatformAdapter {

    private static final String API_BASE = "https://api.github.com";

    @Override
    public RepositoryPlatform getPlatform() {
        return RepositoryPlatform.GITHUB;
    }

    @Override
    public String buildAuthUrl(String webUrl, String accessToken) {
        // GitHub 格式: https://{token}@{host}/{path}.git
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

    @Override
    public boolean validateToken(String webUrl, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/user"))
                    .header("Authorization", "token " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            log.debug("GitHub token validation failed: {}", e.getMessage());
            return false;
        }
    }

}
