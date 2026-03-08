package com.mzfuture.entire.gitsync.adapter;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;

/// Git平台适配器接口
/// 支持不同Git平台的URL构建和认证方式
public interface GitPlatformAdapter {

    /// 获取支持的平台类型
    ///
    /// @return 平台类型枚举
    RepositoryPlatform getPlatform();

    /// 构建带认证的Git URL
    ///
    /// @param webUrl 原始Web URL
    /// @param accessToken 访问令牌
    /// @return 带认证的URL
    String buildAuthUrl(String webUrl, String accessToken);

    /// 构建仓库 Web 界面的单条 commit 查看 URL
    ///
    /// @param webUrl 仓库 Web URL（支持带 .git 后缀，会先规范化）
    /// @param commitSha commit SHA
    /// @return 可点击跳转的 commit 详情页 URL，参数无效时返回 null
    String buildCommitUrl(String webUrl, String commitSha);

    /// 验证token有效性（可选实现）
    ///
    /// @param webUrl 仓库URL
    /// @param accessToken 访问令牌
    /// @return token是否有效
    default boolean validateToken(String webUrl, String accessToken) {
        return true;
    }
}
