package com.mzfuture.entire.gitsync.adapter;

import com.mzfuture.entire.common.exception.Errors;

import java.net.URI;

/// Git URL builder utility (for GitHub/Gitee and other platforms that use token as userinfo)
final class GitUrlUtils {

    private GitUrlUtils() {
    }

    /// Build format: https://{token}@{host}{path}.git
    static String buildTokenInUserInfoUrl(String webUrl, String accessToken) {
        try {
            URI uri = URI.create(webUrl);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw Errors.INVALID_ARGUMENT.toException("Invalid URL format: " + webUrl);
            }
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            if (path.endsWith(".git")) {
                path = path.substring(0, path.length() - 4);
            }
            path = path.replaceAll("/+$", "");
            if (path.isEmpty()) {
                path = "/";
            }
            return String.format("https://%s@%s%s.git", accessToken, host, path);
        } catch (Exception e) {
            throw Errors.INVALID_ARGUMENT.toException("Invalid URL format: " + webUrl);
        }
    }

    /// Build format: https://{username}:{token}@{host}{path}.git (for Gitee and other platforms that require username:token)
    static String buildUsernameTokenUrl(String webUrl, String username, String accessToken) {
        try {
            URI uri = URI.create(webUrl);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw Errors.INVALID_ARGUMENT.toException("Invalid URL format: " + webUrl);
            }
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            if (path.endsWith(".git")) {
                path = path.substring(0, path.length() - 4);
            }
            path = path.replaceAll("/+$", "");
            if (path.isEmpty()) {
                path = "/";
            }
            return String.format("https://%s:%s@%s%s.git", username, accessToken, host, path);
        } catch (Exception e) {
            throw Errors.INVALID_ARGUMENT.toException("Invalid URL format: " + webUrl);
        }
    }

    /// Normalize webUrl for building commit URL: remove .git suffix and trailing slash
    static String normalizeWebUrlForCommit(String webUrl) {
        try {
            URI uri = URI.create(webUrl);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                return null;
            }
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            if (path.endsWith(".git")) {
                path = path.substring(0, path.length() - 4);
            }
            path = path.replaceAll("/+$", "");
            if (path.isEmpty()) {
                path = "/";
            }
            int port = uri.getPort();
            String scheme = uri.getScheme() != null ? uri.getScheme() : "https";
            String portPart = (port > 0 && port != 443 && port != 80) ? ":" + port : "";
            return scheme + "://" + host + portPart + path;
        } catch (Exception e) {
            return null;
        }
    }

    /// Parse API base address from webUrl (for GitLab/Gitee enterprise edition, etc.)
    static String getApiBaseFromWebUrl(String webUrl, String apiPath) {
        try {
            URI uri = URI.create(webUrl);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw Errors.INVALID_ARGUMENT.toException("Invalid URL format: " + webUrl);
            }
            int port = uri.getPort();
            String scheme = uri.getScheme() != null ? uri.getScheme() : "https";
            String portPart = (port > 0 && port != 443 && port != 80) ? ":" + port : "";
            return scheme + "://" + host + portPart + apiPath;
        } catch (Exception e) {
            throw Errors.INVALID_ARGUMENT.toException("Invalid URL format: " + webUrl);
        }
    }
}
