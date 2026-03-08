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

/// GitLab 平台适配器实现（含自建实例，域名从 webUrl 解析）
@Slf4j
@Component
public class GitlabAdapter implements GitPlatformAdapter {

    @Override
    public RepositoryPlatform getPlatform() {
        return RepositoryPlatform.GITLAB;
    }

    @Override
    public String buildCommitUrl(String webUrl, String commitSha) {
        if (webUrl == null || webUrl.isBlank() || commitSha == null || commitSha.isBlank()) {
            return null;
        }
        String base = GitUrlUtils.normalizeWebUrlForCommit(webUrl);
        return base != null ? base + "/-/commit/" + commitSha : null;
    }

    @Override
    public String buildAuthUrl(String webUrl, String accessToken) {
        // GitLab 格式: https://oauth2:{token}@{host}/{path}.git
        try {
            URI uri = URI.create(webUrl);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw Errors.INVALID_ARGUMENT.toException("无效的URL格式: " + webUrl);
            }
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            if (path.endsWith(".git")) {
                path = path.substring(0, path.length() - 4);
            }
            path = path.replaceAll("/+$", "");
            return String.format("https://oauth2:%s@%s%s.git", accessToken, host, path.isEmpty() ? "/" : path);
        } catch (Exception e) {
            throw Errors.INVALID_ARGUMENT.toException("无效的URL格式: " + webUrl);
        }
    }

    @Override
    public boolean validateToken(String webUrl, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        try {
            String apiBase = GitUrlUtils.getApiBaseFromWebUrl(webUrl, "/api/v4");
            String apiUrl = apiBase + "/user";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("PRIVATE-TOKEN", accessToken)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            log.debug("GitLab token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
