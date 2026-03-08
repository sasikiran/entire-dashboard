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

/// Git sync service implementation
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
        // 1. Match repository exactly by webUrl
        Repo repo = repoRepository.findByWebUrl(params.getWebUrl())
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Repository not found: " + params.getWebUrl()));

        // 2. Execute sync (using global default depth)
        return doSync(repo, params.getAccessToken(), params.getBranch(), -1);
    }

    @Override
    public GitSyncResult syncRepositoryById(Long repoId) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Repository does not exist: " + repoId));
        return doSync(repo, null, null, -1);
    }

    @Override
    public GitSyncResult syncRepositoryById(Long repoId, String branch, int cloneDepth) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Repository does not exist: " + repoId));
        return doSync(repo, null, branch, cloneDepth);
    }

    /// Execute sync operation; cloneDepth < 0 means use global default depth
    private GitSyncResult doSync(Repo repo, String requestToken, String requestBranch, int cloneDepth) {
        Long repoId = repo.getId();
        String repoName = repo.getName();
        String webUrl = repo.getWebUrl();
        RepositoryPlatform platform = repo.getPlatform();

        // Check if platform is supported
        GitPlatformAdapter adapter = platformAdapters.get(platform);
        if (adapter == null) {
            throw Errors.INTERNAL_ERROR.toException("Unsupported platform type: " + platform);
        }

        // Prefer token from request, otherwise use token from database
        String accessToken = StrUtil.isNotBlank(requestToken) ? requestToken : repo.getAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            throw Errors.INTERNAL_ERROR.toException("Access token cannot be empty");
        }

        // Determine branch (use default branch when requestBranch is empty and cloneDepth is not specified)
        String branch = StrUtil.isNotBlank(requestBranch) ? requestBranch : gitProperties.getDefaultBranch();

        // Build auth URL
        String authUrl = adapter.buildAuthUrl(webUrl, accessToken);

        log.info("Starting repository sync: repoId={}, name={}, platform={}, branch={}",
                repoId, repoName, platform, branch);

        // Determine if clone or pull
        boolean exists = gitOperationService.isRepositoryExists(repoId);

        GitSyncResult result = new GitSyncResult();
        result.setRepoId(repoId);
        result.setRepoName(repoName);
        result.setBranch(branch);
        result.setLocalPath(gitOperationService.getLocalRepositoryPath(repoId));
        result.setSyncTime(System.currentTimeMillis());

        try {
            if (exists) {
                // Execute pull (with retry)
                PullResult pullResult = executeWithRetry(() ->
                        gitOperationService.pullRepository(repoId, authUrl, branch));

                result.setStatus(GitSyncStatus.SUCCESS.name());
                result.setHasUpdate(pullResult.isUpdated());
                result.setCommitId(pullResult.getCommitId());
                result.setCommitMessage(pullResult.getCommitMessage());

                log.info("Repository sync successful (pull): repoId={}, hasUpdate={}",
                        repoId, pullResult.isUpdated());

            } else {
                // Execute clone (with retry); cloneDepth < 0 uses default depth
                int depth = cloneDepth < 0 ? gitProperties.getCloneDepth() : cloneDepth;
                final int finalDepth = depth;
                executeWithRetry(() -> {
                    gitOperationService.cloneRepository(repoId, authUrl, branch, finalDepth);
                    return null;
                });

                // Get repository status
                var status = gitOperationService.getRepositoryStatus(repoId);

                result.setStatus(GitSyncStatus.SUCCESS.name());
                result.setHasUpdate(true);
                result.setCommitId(status.getCommitId());
                result.setCommitMessage(status.getCommitMessage());

                log.info("Repository sync successful (clone): repoId={}, path={}",
                        repoId, result.getLocalPath());
            }

        } catch (Exception e) {
            log.error("Repository sync failed: repoId={}, error={}", repoId, e.getMessage(), e);

            result.setStatus(GitSyncStatus.FAILED.name());
            result.setErrorMessage(e.getMessage());
            result.setHasUpdate(false);

            throw Errors.INTERNAL_ERROR.toException("Repository sync failed: " + e.getMessage());
        }

        return result;
    }

    /// Execute operation with retry
    private <T> T executeWithRetry(Supplier<T> operation) {
        int maxRetries = gitProperties.getRetryCount();
        int attempt = 0;
        Exception lastException = null;

        while (attempt <= maxRetries) {
            try {
                if (attempt > 0) {
                    log.info("Retry attempt {}...", attempt);
                    // Wait 1 second before retry
                    Thread.sleep(1000);
                }
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Operation failed (attempt {}): {}", attempt, e.getMessage());
            }
        }

        throw Errors.INTERNAL_ERROR.toException(
                "Operation failed after " + maxRetries + " retries: " + lastException.getMessage());
    }

    @Override
    public GitBranchesDTO getBranches(Long repoId) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Repository does not exist: " + repoId));

        GitPlatformAdapter adapter = platformAdapters.get(repo.getPlatform());
        if (adapter == null) {
            throw Errors.INTERNAL_ERROR.toException("Unsupported platform type: " + repo.getPlatform());
        }

        if (StrUtil.isBlank(repo.getAccessToken())) {
            throw Errors.INTERNAL_ERROR.toException("Access token cannot be empty");
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
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Repository does not exist: " + repoId));
        GitPlatformAdapter adapter = platformAdapters.get(repo.getPlatform());
        if (adapter == null) {
            throw Errors.INTERNAL_ERROR.toException("Unsupported platform type: " + repo.getPlatform());
        }
        if (StrUtil.isBlank(repo.getAccessToken())) {
            throw Errors.INTERNAL_ERROR.toException("Access token cannot be empty");
        }
        String authUrl = adapter.buildAuthUrl(repo.getWebUrl(), repo.getAccessToken());
        List<String> remoteBranches = gitOperationService.listRemoteBranches(authUrl);
        if (remoteBranches == null || remoteBranches.isEmpty()) {
            throw Errors.INTERNAL_ERROR.toException("No remote branches");
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
                log.info("checkpoint all branches fetch completed: repoId={}, branches={}", repoId, remoteBranches.size());
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
                log.info("checkpoint clone + all branches fetch completed: repoId={}, cloneBranch={}, branches={}",
                        repoId, cloneBranch, remoteBranches.size());
            }
        } catch (Exception e) {
            log.error("checkpoint all branches sync failed: repoId={}, error={}", repoId, e.getMessage(), e);
            result.setStatus(GitSyncStatus.FAILED.name());
            result.setErrorMessage(e.getMessage());
            result.setHasUpdate(false);
            throw Errors.INTERNAL_ERROR.toException("checkpoint all branches sync failed: " + e.getMessage());
        }
        return result;
    }

    @Override
    public void fetchMetadataBranch(Long repoId, String metadataBranch) {
        Repo repo = repoRepository.findById(repoId)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Repository does not exist: " + repoId));
        GitPlatformAdapter adapter = platformAdapters.get(repo.getPlatform());
        if (adapter == null) {
            throw Errors.INTERNAL_ERROR.toException("Unsupported platform type: " + repo.getPlatform());
        }
        if (StrUtil.isBlank(repo.getAccessToken())) {
            throw Errors.INTERNAL_ERROR.toException("Access token cannot be empty");
        }
        String authUrl = adapter.buildAuthUrl(repo.getWebUrl(), repo.getAccessToken());
        gitOperationService.fetchBranches(repoId, authUrl, List.of(metadataBranch));
        log.debug("checkpoint metadata branch already fetched to latest: repoId={}, branch={}", repoId, metadataBranch);
    }

    @Override
    public TokenValidateResult validateToken(TokenValidateParams params) {
        GitPlatformAdapter adapter = platformAdapters.get(params.getPlatform());
        if (adapter == null) {
            return TokenValidateResult.fail("Unsupported platform type: " + params.getPlatform());
        }
        if (StrUtil.isBlank(params.getAccessToken())) {
            return TokenValidateResult.fail("Access token cannot be empty");
        }
        if (StrUtil.isBlank(params.getWebUrl())) {
            return TokenValidateResult.fail("Web URL cannot be empty");
        }
        boolean valid = adapter.validateToken(params.getWebUrl(), params.getAccessToken());
        return valid ? TokenValidateResult.ok() : TokenValidateResult.fail("Token is invalid or expired");
    }
}
