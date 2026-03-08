package com.mzfuture.entire.gitsync.service.impl;

import cn.hutool.core.io.FileUtil;
import com.mzfuture.entire.config.GitProperties;
import com.mzfuture.entire.gitsync.dto.response.GitStatusDTO;
import com.mzfuture.entire.gitsync.dto.response.PullResult;
import com.mzfuture.entire.gitsync.service.GitOperationService;
import com.mzfuture.entire.common.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/// Git操作服务实现
@Slf4j
@Service
public class GitOperationServiceImpl implements GitOperationService {

    private final GitProperties gitProperties;

    // 仓库操作锁，防止并发操作同一仓库
    private final ConcurrentHashMap<Long, ReentrantLock> repoLocks = new ConcurrentHashMap<>();

    public GitOperationServiceImpl(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @Override
    public String cloneRepository(Long repoId, String authUrl, String branch) {
        return cloneRepository(repoId, authUrl, branch, 0);
    }

    @Override
    public String cloneRepository(Long repoId, String authUrl, String branch, int depth) {
        ReentrantLock lock = getRepoLock(repoId);
        lock.lock();
        try {
            String localPath = getLocalRepositoryPath(repoId);
            File repoDir = new File(localPath);

            // 如果目录已存在，先删除
            if (repoDir.exists()) {
                FileUtil.del(repoDir);
            }

            // 创建目录
            repoDir.mkdirs();

            int effectiveDepth = depth <= 0 ? gitProperties.getCloneDepth() : depth;
            log.info("开始克隆仓库: repoId={}, branch={}, path={}, depth={}", repoId, branch, localPath, effectiveDepth);

            // 解析token用于认证
            CredentialsProvider credentials = parseCredentials(authUrl);

            CloneCommand clone = Git.cloneRepository()
                    .setURI(authUrl)
                    .setDirectory(repoDir)
                    .setBranch(branch)
                    .setBranchesToClone(List.of("refs/heads/" + branch))
                    .setTimeout(gitProperties.getTimeoutSeconds());
            if (effectiveDepth > 0) {
                clone.setDepth(effectiveDepth);
            }

            if (credentials != null) {
                clone.setCredentialsProvider(credentials);
            }

            clone.call();

            log.info("仓库克隆成功: repoId={}, path={}", repoId, localPath);
            return localPath;

        } catch (GitAPIException e) {
            log.error("克隆仓库失败: repoId={}, error={}", repoId, e.getMessage(), e);
            throw Errors.INTERNAL_ERROR.toException("克隆仓库失败: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public PullResult pullRepository(Long repoId, String authUrl, String branch) {
        ReentrantLock lock = getRepoLock(repoId);
        lock.lock();
        try {
            String localPath = getLocalRepositoryPath(repoId);
            File repoDir = new File(localPath);

            if (!repoDir.exists()) {
                throw Errors.INTERNAL_ERROR.toException("本地仓库不存在: " + localPath);
            }

            log.info("开始拉取仓库更新: repoId={}, branch={}", repoId, branch);

            // 解析token用于认证
            CredentialsProvider credentials = parseCredentials(authUrl);

            try (Git git = Git.open(repoDir)) {
                // 先检出目标分支
                git.checkout().setName(branch).call();

                // 拉取更新
                PullCommand pull = git.pull()
                        .setRemoteBranchName(branch)
                        .setTimeout(gitProperties.getTimeoutSeconds());

                if (credentials != null) {
                    pull.setCredentialsProvider(credentials);
                }

                org.eclipse.jgit.api.PullResult pullResult = pull.call();

                // 获取最新commit信息
                RevCommit latestCommit = getLatestCommit(git);

                boolean hasUpdate = pullResult.getMergeResult() != null
                        && pullResult.getMergeResult().getMergeStatus().isSuccessful();

                log.info("仓库拉取完成: repoId={}, hasUpdate={}", repoId, hasUpdate);

                return PullResult.builder()
                        .updated(hasUpdate)
                        .commitId(latestCommit != null ? latestCommit.getName() : null)
                        .commitMessage(latestCommit != null ? latestCommit.getShortMessage() : null)
                        .author(latestCommit != null ? latestCommit.getAuthorIdent().getName() : null)
                        .commitTime(latestCommit != null ? latestCommit.getCommitTime() * 1000L : null)
                        .build();
            }

        } catch (IOException | GitAPIException e) {
            log.error("拉取仓库失败: repoId={}, error={}", repoId, e.getMessage(), e);
            throw Errors.INTERNAL_ERROR.toException("拉取仓库失败: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public GitStatusDTO getRepositoryStatus(Long repoId) {
        String localPath = getLocalRepositoryPath(repoId);
        File repoDir = new File(localPath);

        GitStatusDTO status = new GitStatusDTO();
        status.setRepoId(repoId);
        status.setLocalPath(localPath);
        status.setExists(repoDir.exists() && new File(localPath + "/.git").exists());

        if (!status.getExists()) {
            return status;
        }

        try (Git git = Git.open(repoDir)) {
            Repository repository = git.getRepository();

            // 获取当前分支
            Ref head = repository.exactRef("HEAD");
            if (head != null && head.isSymbolic()) {
                String branchName = Repository.shortenRefName(head.getTarget().getName());
                status.setCurrentBranch(branchName);
            }

            // 获取最新commit
            RevCommit latestCommit = getLatestCommit(git);
            if (latestCommit != null) {
                status.setCommitId(latestCommit.getName());
                status.setCommitMessage(latestCommit.getShortMessage());
                status.setLastCommitTime(latestCommit.getCommitTime() * 1000L);
            }

        } catch (IOException e) {
            log.warn("获取仓库状态失败: repoId={}, error={}", repoId, e.getMessage());
        }

        return status;
    }

    @Override
    public boolean isRepositoryExists(Long repoId) {
        String localPath = getLocalRepositoryPath(repoId);
        File repoDir = new File(localPath);
        File gitDir = new File(localPath + "/.git");
        return repoDir.exists() && gitDir.exists();
    }

    @Override
    public String getLocalRepositoryPath(Long repoId) {
        String dataPath = gitProperties.getDataPath();
        // 确保路径以/结尾
        if (!dataPath.endsWith("/") && !dataPath.endsWith("\\")) {
            dataPath = dataPath + "/";
        }
        return dataPath + repoId;
    }

    @Override
    public boolean deleteRepository(Long repoId) {
        ReentrantLock lock = getRepoLock(repoId);
        lock.lock();
        try {
            String localPath = getLocalRepositoryPath(repoId);
            File repoDir = new File(localPath);

            if (!repoDir.exists()) {
                return true;
            }

            log.info("删除本地仓库: repoId={}, path={}", repoId, localPath);
            return FileUtil.del(repoDir);

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void fetchBranches(Long repoId, String authUrl, List<String> branchNames) {
        if (branchNames == null || branchNames.isEmpty()) {
            return;
        }
        ReentrantLock lock = getRepoLock(repoId);
        lock.lock();
        try {
            String localPath = getLocalRepositoryPath(repoId);
            File repoDir = new File(localPath);
            if (!repoDir.exists() || !new File(repoDir, ".git").exists()) {
                throw Errors.INTERNAL_ERROR.toException("本地仓库不存在: " + localPath);
            }
            CredentialsProvider credentials = parseCredentials(authUrl);
            List<RefSpec> refSpecs = new ArrayList<>();
            for (String branch : branchNames) {
                refSpecs.add(new RefSpec("refs/heads/" + branch + ":refs/heads/" + branch));
            }
            try (Git git = Git.open(repoDir)) {
                FetchCommand fetch = git.fetch()
                        .setRemote("origin")
                        .setRefSpecs(refSpecs)
                        .setTimeout(gitProperties.getTimeoutSeconds());
                if (credentials != null) {
                    fetch.setCredentialsProvider(credentials);
                }
                fetch.call();
                log.info("fetch 分支完成: repoId={}, branches={}", repoId, branchNames.size());
            }
        } catch (IOException | GitAPIException e) {
            log.error("fetch 分支失败: repoId={}, error={}", repoId, e.getMessage(), e);
            throw Errors.INTERNAL_ERROR.toException("fetch 分支失败: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<String> listRemoteBranches(String authUrl) {
        try {
            CredentialsProvider credentials = parseCredentials(authUrl);
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setRemote(authUrl)
                    .setCredentialsProvider(credentials)
                    .setHeads(true)
                    .setTags(false)
                    .setTimeout(gitProperties.getTimeoutSeconds())
                    .call();

            return refs.stream()
                    .map(ref -> Repository.shortenRefName(ref.getName()))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (GitAPIException e) {
            log.error("获取远程分支列表失败: error={}", e.getMessage(), e);
            throw Errors.INTERNAL_ERROR.toException("获取远程分支列表失败: " + e.getMessage());
        }
    }

    /// 获取仓库操作锁
    private ReentrantLock getRepoLock(Long repoId) {
        return repoLocks.computeIfAbsent(repoId, k -> new ReentrantLock());
    }

    /// 解析认证信息
    private CredentialsProvider parseCredentials(String authUrl) {
        // 从URL中解析token
        // 格式: https://oauth2:token@host/path 或 https://token@host/path
        try {
            java.net.URL url = new java.net.URL(authUrl);
            String userInfo = url.getUserInfo();
            if (userInfo != null && !userInfo.isEmpty()) {
                String[] parts = userInfo.split(":");
                if (parts.length >= 2) {
                    // oauth2:token格式
                    return new UsernamePasswordCredentialsProvider(parts[0], parts[1]);
                } else {
                    // 只有token格式
                    return new UsernamePasswordCredentialsProvider(userInfo, "");
                }
            }
        } catch (java.net.MalformedURLException e) {
            log.warn("解析URL失败: {}", authUrl);
        }
        return null;
    }

    /// 获取最新commit
    private RevCommit getLatestCommit(Git git) throws IOException {
        try {
            Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
            for (RevCommit commit : commits) {
                return commit;
            }
        } catch (GitAPIException e) {
            log.warn("获取最新commit失败: {}", e.getMessage());
        }
        return null;
    }
}
