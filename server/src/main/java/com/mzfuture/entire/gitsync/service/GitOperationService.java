package com.mzfuture.entire.gitsync.service;

import com.mzfuture.entire.gitsync.dto.response.GitStatusDTO;
import com.mzfuture.entire.gitsync.dto.response.PullResult;

/// Git操作服务接口
/// 提供基础的Git操作功能（clone、pull、status等）
public interface GitOperationService {

    /// 克隆仓库（单分支浅克隆）
    ///
    /// @param repoId 仓库ID
    /// @param authUrl 带认证的URL
    /// @param branch 分支名
    /// @return 本地仓库路径
    String cloneRepository(Long repoId, String authUrl, String branch);

    /// 克隆仓库（可指定深度，depth<=0 表示使用全局默认深度）
    ///
    /// @param repoId 仓库ID
    /// @param authUrl 带认证的URL
    /// @param branch 分支名
    /// @param depth 克隆深度，0=全量，>0=浅克隆深度
    /// @return 本地仓库路径
    String cloneRepository(Long repoId, String authUrl, String branch, int depth);

    /// 拉取最新代码
    ///
    /// @param repoId 仓库ID
    /// @param authUrl 带认证的URL
    /// @param branch 分支名
    /// @return Pull操作结果
    PullResult pullRepository(Long repoId, String authUrl, String branch);

    /// 获取仓库当前状态
    ///
    /// @param repoId 仓库ID
    /// @return 仓库状态信息
    GitStatusDTO getRepositoryStatus(Long repoId);

    /// 检查本地仓库是否存在
    ///
    /// @param repoId 仓库ID
    /// @return 是否存在
    boolean isRepositoryExists(Long repoId);

    /// 获取本地仓库路径
    ///
    /// @param repoId 仓库ID
    /// @return 本地路径
    String getLocalRepositoryPath(Long repoId);

    /// 删除本地仓库
    ///
    /// @param repoId 仓库ID
    /// @return 是否成功删除
    boolean deleteRepository(Long repoId);

    /// 获取远程仓库所有分支名称
    ///
    /// @param authUrl 带认证的URL
    /// @return 分支名称列表
    java.util.List<String> listRemoteBranches(String authUrl);

    /// 在已有本地仓库上 fetch 指定分支（不切换 checkout），用于 checkpoint 多分支同步
    ///
    /// @param repoId 仓库ID
    /// @param authUrl 带认证的URL
    /// @param branchNames 要 fetch 的分支名列表
    void fetchBranches(Long repoId, String authUrl, java.util.List<String> branchNames);
}
