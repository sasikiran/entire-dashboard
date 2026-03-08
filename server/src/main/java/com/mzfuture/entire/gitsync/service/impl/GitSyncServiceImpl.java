package com.mzfuture.entire.gitsync.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mzfuture.entire.config.GitProperties;
import com.mzfuture.entire.gitrepo.entity.Repo;
import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import com.mzfuture.entire.gitrepo.repository.RepoRepository;
import com.mzfuture.entire.gitsync.dto.request.GitSyncParams;
import com.mzfuture.entire.gitsync.dto.request.TokenValidateParams;
import com.mzfuture.entire.gitsync.dto.response.GitBranchesDTO;
import com.mzfuture.entire.gitsync.dto.response.GitSyncResult;
import com.mzfuture.entire.gitsync.dto.response.TokenValidateResult;
import com.mzfuture.entire.gitsync.dto.response.PullResult;
import com.mzfuture.entire.gitsync.enums.GitSyncStatus;
import com.mzfuture.entire.gitsync.adapter.GitPlatformAdapter;
import com.mzfuture.entire.gitsync.service.GitOperationService;
import com.mzfuture.entire.gitsync.service.GitSyncService;
import com.mzfuture.entire.common.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/// Git同步服务实现
@Slf4j
@Service
public class GitSyncServiceImpl implements GitSyncService {

    private final RepoRepository repoRepository;
    private final GitOperationService gitOperationService;
    private final GitProperties gitProperties;
    private final Map<RepositoryPlatform, GitPlatformAdapter> platformAdapters;

    public GitSyncServiceImpl(
            RepoRepository repoRepository,
            GitOperationService gitOperationService,
            GitProperties gitProperties,
            List<GitPlatformAdapter> adapters) {
        this.repoRepository = repoRepository;
        this.gitOperationService = gitOperationService;
        this.gitProperties = gitProperties;
        this.platformAdapters = adapters.stream()
                .collect(Collectors.toMap(GitPlatformAdapter::getPlatform, adapter -> adapter));
    }

    @Override
    public GitSyncResult syncRepository(GitSyncParams params) {
        // 1. 根据webUrl精确匹配仓库
        Repo repo = repoRepository.findByWebUrl(params.getWebUrl())
                .orElseThrow(() -> Errors.NOT_FOUND.toException("未找到匹配的仓库: " + params.getWebUrl()));

        // 2. 执行同步（使用全局默认深度）
        return doSync(repo, params.getAccessToken(), params.getBranch(), -1);
    }

    @Override
    public GitSyncResult syncRepositoryById(Long repoId) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("仓库不存在: " + repoId));
        return doSync(repo, null, null, -1);
    }

    @Override
    public GitSyncResult syncRepositoryById(Long repoId, String branch, int cloneDepth) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("仓库不存在: " + repoId));
        return doSync(repo, null, branch, cloneDepth);
    }

    /// 执行同步操作；cloneDepth < 0 表示使用全局默认深度
    private GitSyncResult doSync(Repo repo, String requestToken, String requestBranch, int cloneDepth) {
        Long repoId = repo.getId();
        String repoName = repo.getName();
        String webUrl = repo.getWebUrl();
        RepositoryPlatform platform = repo.getPlatform();

        // 检查平台是否支持
        GitPlatformAdapter adapter = platformAdapters.get(platform);
        if (adapter == null) {
            throw Errors.INTERNAL_ERROR.toException("暂不支持的平台类型: " + platform);
        }

        // 优先使用请求中的token，否则使用数据库中的token
        String accessToken = StrUtil.isNotBlank(requestToken) ? requestToken : repo.getAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            throw Errors.INTERNAL_ERROR.toException("访问令牌不能为空");
        }

        // 确定分支（requestBranch 为空且 cloneDepth 未指定时用默认分支）
        String branch = StrUtil.isNotBlank(requestBranch) ? requestBranch : gitProperties.getDefaultBranch();

        // 构建认证URL
        String authUrl = adapter.buildAuthUrl(webUrl, accessToken);

        log.info("开始同步仓库: repoId={}, name={}, platform={}, branch={}",
                repoId, repoName, platform, branch);

        // 判断是clone还是pull
        boolean exists = gitOperationService.isRepositoryExists(repoId);

        GitSyncResult result = new GitSyncResult();
        result.setRepoId(repoId);
        result.setRepoName(repoName);
        result.setBranch(branch);
        result.setLocalPath(gitOperationService.getLocalRepositoryPath(repoId));
        result.setSyncTime(System.currentTimeMillis());

        try {
            if (exists) {
                // 执行pull（带重试）
                PullResult pullResult = executeWithRetry(() ->
                        gitOperationService.pullRepository(repoId, authUrl, branch));

                result.setStatus(GitSyncStatus.SUCCESS.name());
                result.setHasUpdate(pullResult.isUpdated());
                result.setCommitId(pullResult.getCommitId());
                result.setCommitMessage(pullResult.getCommitMessage());

                log.info("仓库同步成功（pull）: repoId={}, hasUpdate={}",
                        repoId, pullResult.isUpdated());

            } else {
                // 执行clone（带重试）；cloneDepth < 0 使用默认深度
                int depth = cloneDepth < 0 ? gitProperties.getCloneDepth() : cloneDepth;
                final int finalDepth = depth;
                executeWithRetry(() -> {
                    gitOperationService.cloneRepository(repoId, authUrl, branch, finalDepth);
                    return null;
                });

                // 获取仓库状态
                var status = gitOperationService.getRepositoryStatus(repoId);

                result.setStatus(GitSyncStatus.SUCCESS.name());
                result.setHasUpdate(true);
                result.setCommitId(status.getCommitId());
                result.setCommitMessage(status.getCommitMessage());

                log.info("仓库同步成功（clone）: repoId={}, path={}",
                        repoId, result.getLocalPath());
            }

        } catch (Exception e) {
            log.error("仓库同步失败: repoId={}, error={}", repoId, e.getMessage(), e);

            result.setStatus(GitSyncStatus.FAILED.name());
            result.setErrorMessage(e.getMessage());
            result.setHasUpdate(false);

            throw Errors.INTERNAL_ERROR.toException("仓库同步失败: " + e.getMessage());
        }

        return result;
    }

    /// 带重试执行操作
    private <T> T executeWithRetry(Supplier<T> operation) {
        int maxRetries = gitProperties.getRetryCount();
        int attempt = 0;
        Exception lastException = null;

        while (attempt <= maxRetries) {
            try {
                if (attempt > 0) {
                    log.info("第{}次重试...", attempt);
                    // 重试前等待1秒
                    Thread.sleep(1000);
                }
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("操作失败（第{}次尝试）: {}", attempt, e.getMessage());
            }
        }

        throw Errors.INTERNAL_ERROR.toException(
                "操作失败，已重试" + maxRetries + "次: " + lastException.getMessage());
    }

    @Override
    public GitBranchesDTO getBranches(Long repoId) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("仓库不存在: " + repoId));

        GitPlatformAdapter adapter = platformAdapters.get(repo.getPlatform());
        if (adapter == null) {
            throw Errors.INTERNAL_ERROR.toException("暂不支持的平台类型: " + repo.getPlatform());
        }

        if (StrUtil.isBlank(repo.getAccessToken())) {
            throw Errors.INTERNAL_ERROR.toException("访问令牌不能为空");
        }

        String authUrl = adapter.buildAuthUrl(repo.getWebUrl(), repo.getAccessToken());

        List<String> branches = gitOperationService.listRemoteBranches(authUrl);

        GitBranchesDTO result = new GitBranchesDTO();
        result.setRepoId(repoId);
        result.setBranches(branches);
        return result;
    }

    @Override
    public GitSyncResult syncRepositoryAllBranchesForCheckpoint(Long repoId, String metadataBranch, int cloneDepth) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("仓库不存在: " + repoId));
        GitPlatformAdapter adapter = platformAdapters.get(repo.getPlatform());
        if (adapter == null) {
            throw Errors.INTERNAL_ERROR.toException("暂不支持的平台类型: " + repo.getPlatform());
        }
        if (StrUtil.isBlank(repo.getAccessToken())) {
            throw Errors.INTERNAL_ERROR.toException("访问令牌不能为空");
        }
        String authUrl = adapter.buildAuthUrl(repo.getWebUrl(), repo.getAccessToken());
        List<String> remoteBranches = gitOperationService.listRemoteBranches(authUrl);
        if (remoteBranches == null || remoteBranches.isEmpty()) {
            throw Errors.INTERNAL_ERROR.toException("远程无分支");
        }

        GitSyncResult result = new GitSyncResult();
        result.setRepoId(repoId);
        result.setRepoName(repo.getName());
        result.setBranch(metadataBranch);
        result.setLocalPath(gitOperationService.getLocalRepositoryPath(repoId));
        result.setSyncTime(System.currentTimeMillis());

        boolean exists = gitOperationService.isRepositoryExists(repoId);
        try {
            if (exists) {
                gitOperationService.fetchBranches(repoId, authUrl, remoteBranches);
                result.setStatus(GitSyncStatus.SUCCESS.name());
                result.setHasUpdate(true);
                log.info("checkpoint 全分支 fetch 完成: repoId={}, branches={}", repoId, remoteBranches.size());
            } else {
                String cloneBranch = remoteBranches.contains(metadataBranch) ? metadataBranch : remoteBranches.get(0);
                int depth = cloneDepth < 0 ? gitProperties.getCloneDepth() : cloneDepth;
                executeWithRetry(() -> {
                    gitOperationService.cloneRepository(repoId, authUrl, cloneBranch, depth);
                    return null;
                });
                gitOperationService.fetchBranches(repoId, authUrl, remoteBranches);
                result.setStatus(GitSyncStatus.SUCCESS.name());
                result.setHasUpdate(true);
                log.info("checkpoint 克隆+全分支 fetch 完成: repoId={}, cloneBranch={}, branches={}",
                        repoId, cloneBranch, remoteBranches.size());
            }
        } catch (Exception e) {
            log.error("checkpoint 全分支同步失败: repoId={}, error={}", repoId, e.getMessage(), e);
            result.setStatus(GitSyncStatus.FAILED.name());
            result.setErrorMessage(e.getMessage());
            result.setHasUpdate(false);
            throw Errors.INTERNAL_ERROR.toException("checkpoint 全分支同步失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public void fetchMetadataBranch(Long repoId, String metadataBranch) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("仓库不存在: " + repoId));
        GitPlatformAdapter adapter = platformAdapters.get(repo.getPlatform());
        if (adapter == null) {
            throw Errors.INTERNAL_ERROR.toException("暂不支持的平台类型: " + repo.getPlatform());
        }
        if (StrUtil.isBlank(repo.getAccessToken())) {
            throw Errors.INTERNAL_ERROR.toException("访问令牌不能为空");
        }
        String authUrl = adapter.buildAuthUrl(repo.getWebUrl(), repo.getAccessToken());
        gitOperationService.fetchBranches(repoId, authUrl, List.of(metadataBranch));
        log.debug("checkpoint metadata 分支已拉取最新: repoId={}, branch={}", repoId, metadataBranch);
    }

    @Override
    public TokenValidateResult validateToken(TokenValidateParams params) {
        GitPlatformAdapter adapter = platformAdapters.get(params.getPlatform());
        if (adapter == null) {
            return TokenValidateResult.fail("暂不支持的平台类型: " + params.getPlatform());
        }
        if (StrUtil.isBlank(params.getAccessToken())) {
            return TokenValidateResult.fail("访问令牌不能为空");
        }
        if (StrUtil.isBlank(params.getWebUrl())) {
            return TokenValidateResult.fail("Web URL 不能为空");
        }
        boolean valid = adapter.validateToken(params.getWebUrl(), params.getAccessToken());
        return valid ? TokenValidateResult.ok() : TokenValidateResult.fail("Token 无效或已过期");
    }
}
