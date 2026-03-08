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

/// Gitee platform adapter implementation (including enterprise edition, domain parsed from webUrl)
/// Gitee Git over HTTPS requires username:token format, need to fetch current user login via API
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
        // Gitee Git authentication requires username:token format, fetch current user login via /user API
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

    /// Fetch current user login via Gitee API (username of the token owner account)
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
                throw Errors.INTERNAL_ERROR.toException("Gitee API returned " + response.statusCode() + ", cannot get user info");
            }

            JsonNode root = OBJECT_MAPPER.readTree(response.body());
            JsonNode login = root.get("login");
            if (login == null || !login.isTextual()) {
                throw Errors.INTERNAL_ERROR.toException("Gitee user info missing login field");
            }
            return login.asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get Gitee username: {}", e.getMessage());
            throw Errors.INTERNAL_ERROR.toException("Failed to get Gitee username: " + e.getMessage());
        }
    }
}
