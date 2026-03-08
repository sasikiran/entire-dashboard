package com.mzfuture.entire.gitsync.service;

import com.mzfuture.entire.gitsync.dto.request.GitSyncParams;
import com.mzfuture.entire.gitsync.dto.request.TokenValidateParams;
import com.mzfuture.entire.gitsync.dto.response.GitBranchesDTO;
import com.mzfuture.entire.gitsync.dto.response.GitSyncResult;
import com.mzfuture.entire.gitsync.dto.response.TokenValidateResult;

/// Git同步服务接口
/// 提供高层同步功能，包括重试、错误处理等
public interface GitSyncService {

    /// 同步仓库代码
    ///
    /// @param params 同步参数
    /// @return 同步结果
    GitSyncResult syncRepository(GitSyncParams params);

    /// 根据Repo ID同步（使用数据库中存储的配置）
    ///
    /// @param repoId 仓库ID
    /// @return 同步结果
    GitSyncResult syncRepositoryById(Long repoId);

    /// 根据Repo ID同步，指定分支与克隆深度（用于 checkpoint 同步等）
    ///
    /// @param repoId 仓库ID
    /// @param branch 分支名，null 使用默认
    /// @param cloneDepth 克隆深度，0=全量，>0=浅克隆；负数使用全局默认
    /// @return 同步结果
    GitSyncResult syncRepositoryById(Long repoId, String branch, int cloneDepth);

    /// 获取仓库所有分支列表
    ///
    /// @param repoId 仓库ID
    /// @return 分支列表信息
    GitBranchesDTO getBranches(Long repoId);

    /// 为 checkpoint 同步拉取全部分支：本地无仓库则先 clone metadata 分支再 fetch 所有分支，已有仓库则 fetch 所有分支（不依赖当前 HEAD）
    ///
    /// @param repoId 仓库ID
    /// @param metadataBranch 仅存 checkpoint 元数据的分支名（如 entire/checkpoints/v1），clone 时优先使用
    /// @param cloneDepth 克隆深度，0=全量，>0=浅克隆；负数使用全局默认
    /// @return 同步结果
    GitSyncResult syncRepositoryAllBranchesForCheckpoint(Long repoId, String metadataBranch, int cloneDepth);

    /// 将 metadata 分支拉取到最新（仅 fetch 该分支），在 walk 内容分支解析 checkpoint 前调用，确保读到最新 metadata 文件
    ///
    /// @param repoId 仓库ID
    /// @param metadataBranch 元数据分支名（如 entire/checkpoints/v1）
    void fetchMetadataBranch(Long repoId, String metadataBranch);

    /// 校验访问令牌有效性（调用各平台 API）
    ///
    /// @param params webUrl、platform、accessToken
    /// @return 校验结果
    TokenValidateResult validateToken(TokenValidateParams params);
}
