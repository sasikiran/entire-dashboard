package com.mzfuture.entire.gitsync.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import com.mzfuture.entire.common.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/// Gitee 平台适配器实现（含企业版，域名从 webUrl 解析）
/// Gitee Git over HTTPS 需要 username:token 格式，需通过 API 获取当前用户 login
@Slf4j
@Component
public class GiteeAdapter implements GitPlatformAdapter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public RepositoryPlatform getPlatform() {
        return RepositoryPlatform.GITEE;
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
    public String buildAuthUrl(String webUrl, String accessToken) {
        // Gitee Git 认证需要 username:token 格式，通过 /user API 获取当前用户 login
        String username = fetchGiteeUsername(webUrl, accessToken);
        return GitUrlUtils.buildUsernameTokenUrl(webUrl, username, accessToken);
    }

    @Override
    public boolean validateToken(String webUrl, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        try {
            String apiBase = getApiBaseFromWebUrl(webUrl);
            String apiUrl = apiBase + "/user?access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            log.debug("Gitee token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private String getApiBaseFromWebUrl(String webUrl) {
        return GitUrlUtils.getApiBaseFromWebUrl(webUrl, "/api/v5");
    }

    /// 通过 Gitee API 获取当前用户 login（token 所属账户的用户名）
    private String fetchGiteeUsername(String webUrl, String accessToken) {
        try {
            String apiBase = getApiBaseFromWebUrl(webUrl);
            String apiUrl = apiBase + "/user?access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw Errors.INTERNAL_ERROR.toException("Gitee API 返回 " + response.statusCode() + "，无法获取用户信息");
            }

            JsonNode root = OBJECT_MAPPER.readTree(response.body());
            JsonNode login = root.get("login");
            if (login == null || !login.isTextual()) {
                throw Errors.INTERNAL_ERROR.toException("Gitee 用户信息中缺少 login 字段");
            }
            return login.asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 Gitee 用户名失败: {}", e.getMessage());
            throw Errors.INTERNAL_ERROR.toException("获取 Gitee 用户名失败: " + e.getMessage());
        }
    }
}
