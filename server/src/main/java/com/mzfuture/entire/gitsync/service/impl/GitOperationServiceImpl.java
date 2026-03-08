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

/// Git operation service implementation
@Slf4j
@Service
public class GitOperationServiceImpl implements GitOperationService {

    private final GitProperties gitProperties;

    // Repository operation lock, preventing concurrent operations on the same repository
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

            // If directory already exists, delete first
            if (repoDir.exists()) {
                FileUtil.del(repoDir);
            }

            // Create directory
            repoDir.mkdirs();

            int effectiveDepth = depth <= 0 ? gitProperties.getCloneDepth() : depth;
            log.info("Starting to clone repository: repoId={}, branch={}, path={}, depth={}", repoId, branch, localPath, effectiveDepth);

            // Parse token for authentication
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

            log.info("Repository cloned successfully: repoId={}, path={}", repoId, localPath);
            return localPath;

        } catch (GitAPIException e) {
            log.error("Failed to clone repository: repoId={}, error={}", repoId, e.getMessage(), e);
            throw Errors.INTERNAL_ERROR.toException("Failed to clone repository: " + e.getMessage());
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
                throw Errors.INTERNAL_ERROR.toException("Local repository does not exist: " + localPath);
            }

            log.info("Starting to pull repository updates: repoId={}, branch={}", repoId, branch);

            // Parse token for authentication
            CredentialsProvider credentials = parseCredentials(authUrl);

            try (Git git = Git.open(repoDir)) {
                // First checkout target branch
                git.checkout().setName(branch).call();

                // Pull updates
                PullCommand pull = git.pull()
                        .setRemoteBranchName(branch)
                        .setTimeout(gitProperties.getTimeoutSeconds());

                if (credentials != null) {
                    pull.setCredentialsProvider(credentials);
                }

                org.eclipse.jgit.api.PullResult pullResult = pull.call();

                // Get latest commit information
                RevCommit latestCommit = getLatestCommit(git);

                boolean hasUpdate = pullResult.getMergeResult() != null
                        && pullResult.getMergeResult().getMergeStatus().isSuccessful();

                log.info("Repository pull completed: repoId={}, hasUpdate={}", repoId, hasUpdate);

                return PullResult.builder()
                        .updated(hasUpdate)
                        .commitId(latestCommit != null ? latestCommit.getName() : null)
                        .commitMessage(latestCommit != null ? latestCommit.getShortMessage() : null)
                        .author(latestCommit != null ? latestCommit.getAuthorIdent().getName() : null)
                        .commitTime(latestCommit != null ? latestCommit.getCommitTime() * 1000L : null)
                        .build();
            }

        } catch (IOException | GitAPIException e) {
            log.error("Failed to pull repository: repoId={}, error={}", repoId, e.getMessage(), e);
            throw Errors.INTERNAL_ERROR.toException("Failed to pull repository: " + e.getMessage());
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

            // Get current branch
            Ref head = repository.exactRef("HEAD");
            if (head != null && head.isSymbolic()) {
                String branchName = Repository.shortenRefName(head.getTarget().getName());
                status.setCurrentBranch(branchName);
            }

            // Get latest commit
            RevCommit latestCommit = getLatestCommit(git);
            if (latestCommit != null) {
                status.setCommitId(latestCommit.getName());
                status.setCommitMessage(latestCommit.getShortMessage());
                status.setLastCommitTime(latestCommit.getCommitTime() * 1000L);
            }

        } catch (IOException e) {
            log.warn("Failed to get repository status: repoId={}, error={}", repoId, e.getMessage());
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
        // Ensure path ends with /
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

            log.info("Deleting local repository: repoId={}, path={}", repoId, localPath);
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
                throw Errors.INTERNAL_ERROR.toException("Local repository does not exist: " + localPath);
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
                log.info("Fetch branches completed: repoId={}, branches={}", repoId, branchNames.size());
            }
        } catch (IOException | GitAPIException e) {
            log.error("Failed to fetch branches: repoId={}, error={}", repoId, e.getMessage(), e);
            throw Errors.INTERNAL_ERROR.toException("Failed to fetch branches: " + e.getMessage());
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
            log.error("Failed to get remote branch list: error={}", e.getMessage(), e);
            throw Errors.INTERNAL_ERROR.toException("Failed to get remote branch list: " + e.getMessage());
        }
    }

    /// Get repository operation lock
    private ReentrantLock getRepoLock(Long repoId) {
        return repoLocks.computeIfAbsent(repoId, k -> new ReentrantLock());
    }

    /// Parse authentication information
    private CredentialsProvider parseCredentials(String authUrl) {
        // Parse token from URL
        // Format: https://oauth2:token@host/path or https://token@host/path
        try {
            java.net.URL url = new java.net.URL(authUrl);
            String userInfo = url.getUserInfo();
            if (userInfo != null && !userInfo.isEmpty()) {
                String[] parts = userInfo.split(":");
                if (parts.length >= 2) {
                    // oauth2:token format
                    return new UsernamePasswordCredentialsProvider(parts[0], parts[1]);
                } else {
                    // Token-only format
                    return new UsernamePasswordCredentialsProvider(userInfo, "");
                }
            }
        } catch (java.net.MalformedURLException e) {
            log.warn("Failed to parse URL: {}", authUrl);
        }
        return null;
    }

    /// Get latest commit
    private RevCommit getLatestCommit(Git git) throws IOException {
        try {
            Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
            for (RevCommit commit : commits) {
                return commit;
            }
        } catch (GitAPIException e) {
            log.warn("Failed to get latest commit: {}", e.getMessage());
        }
        return null;
    }
}
