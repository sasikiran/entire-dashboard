package com.mzfuture.entire.gitsync.adapter;

import com.mzfuture.entire.common.exception.Errors;

import java.net.URI;

/// Git URL 构建工具（GitHub/Gitee 等使用 token 作为 userinfo 的平台）
final class GitUrlUtils {

    private GitUrlUtils() {
    }

    /// 构建格式: https://{token}@{host}{path}.git
    static String buildTokenInUserInfoUrl(String webUrl, String accessToken) {
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
            if (path.isEmpty()) {
                path = "/";
            }
            return String.format("https://%s@%s%s.git", accessToken, host, path);
        } catch (Exception e) {
            throw Errors.INVALID_ARGUMENT.toException("无效的URL格式: " + webUrl);
        }
    }

    /// 构建格式: https://{username}:{token}@{host}{path}.git（Gitee 等需要 username:token 的平台）
    static String buildUsernameTokenUrl(String webUrl, String username, String accessToken) {
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
            if (path.isEmpty()) {
                path = "/";
            }
            return String.format("https://%s:%s@%s%s.git", username, accessToken, host, path);
        } catch (Exception e) {
            throw Errors.INVALID_ARGUMENT.toException("无效的URL格式: " + webUrl);
        }
    }

    /// 规范化 webUrl 用于构建 commit URL：去除 .git 后缀和末尾斜杠
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

    /// 从 webUrl 解析 API 基础地址（用于 GitLab/Gitee 企业版等）
    static String getApiBaseFromWebUrl(String webUrl, String apiPath) {
        try {
            URI uri = URI.create(webUrl);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw Errors.INVALID_ARGUMENT.toException("无效的URL格式: " + webUrl);
            }
            int port = uri.getPort();
            String scheme = uri.getScheme() != null ? uri.getScheme() : "https";
            String portPart = (port > 0 && port != 443 && port != 80) ? ":" + port : "";
            return scheme + "://" + host + portPart + apiPath;
        } catch (Exception e) {
            throw Errors.INVALID_ARGUMENT.toException("无效的URL格式: " + webUrl);
        }
    }
}
