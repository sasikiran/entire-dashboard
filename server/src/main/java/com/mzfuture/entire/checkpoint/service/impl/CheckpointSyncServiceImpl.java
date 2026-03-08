package com.mzfuture.entire.checkpoint.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mzfuture.entire.checkpoint.config.CheckpointSyncProperties;
import com.mzfuture.entire.checkpoint.entity.Checkpoint;
import com.mzfuture.entire.checkpoint.entity.CheckpointSyncState;
import com.mzfuture.entire.checkpoint.git.CheckpointGitReader;
import com.mzfuture.entire.checkpoint.entity.Session;
import com.mzfuture.entire.checkpoint.parser.CheckpointMetadataParser;
import com.mzfuture.entire.checkpoint.parser.CheckpointParseResult;
import com.mzfuture.entire.checkpoint.parser.SessionMetadataParser;
import com.mzfuture.entire.checkpoint.parser.SessionParseResult;
import com.mzfuture.entire.checkpoint.repository.CheckpointRepository;
import com.mzfuture.entire.checkpoint.repository.CheckpointSyncStateRepository;
import com.mzfuture.entire.checkpoint.repository.SessionRepository;
import com.mzfuture.entire.checkpoint.service.CheckpointSyncService;
import com.mzfuture.entire.gitrepo.entity.Repo;
import com.mzfuture.entire.gitrepo.repository.RepoRepository;
import com.mzfuture.entire.gitsync.dto.response.GitBranchesDTO;
import com.mzfuture.entire.gitsync.service.GitSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CheckpointSyncServiceImpl implements CheckpointSyncService {

    private static final Pattern TRAILER_PATTERN = Pattern.compile("Entire-Checkpoint:\\s*([0-9a-f]{12})", Pattern.CASE_INSENSITIVE);

    private final RepoRepository repoRepository;
    private final CheckpointSyncStateRepository syncStateRepository;
    private final CheckpointRepository checkpointRepository;
    private final SessionRepository sessionRepository;
    private final GitSyncService gitSyncService;
    private final CheckpointGitReader gitReader;
    private final CheckpointMetadataParser metadataParser;
    private final SessionMetadataParser sessionMetadataParser;
    private final CheckpointSyncProperties syncProperties;

    public CheckpointSyncServiceImpl(
            RepoRepository repoRepository,
            CheckpointSyncStateRepository syncStateRepository,
            CheckpointRepository checkpointRepository,
            SessionRepository sessionRepository,
            GitSyncService gitSyncService,
            CheckpointGitReader gitReader,
            CheckpointMetadataParser metadataParser,
            SessionMetadataParser sessionMetadataParser,
            CheckpointSyncProperties syncProperties) {
        this.repoRepository = repoRepository;
        this.syncStateRepository = syncStateRepository;
        this.checkpointRepository = checkpointRepository;
        this.sessionRepository = sessionRepository;
        this.gitSyncService = gitSyncService;
        this.gitReader = gitReader;
        this.metadataParser = metadataParser;
        this.sessionMetadataParser = sessionMetadataParser;
        this.syncProperties = syncProperties;
    }

    @Override
    @Transactional
    public void syncRepo(Long repoId, boolean fullScan) {
        Repo repo = repoRepository.findById(repoId).orElse(null);
        if (repo == null) {
            log.warn("Checkpoint sync: repo not found, repoId={}", repoId);
            return;
        }
        String metadataBranch = syncProperties.getBranch();
        try {
            if (!remoteHasBranch(repoId, metadataBranch)) {
                log.info("Checkpoint sync: repo {} has no metadata branch {}, skip", repoId, metadataBranch);
                return;
            }
        } catch (Exception e) {
            log.warn("Checkpoint sync: list branches failed for repoId={}, error={}", repoId, e.getMessage());
            return;
        }

        try {
            gitSyncService.syncRepositoryAllBranchesForCheckpoint(repoId, metadataBranch, syncProperties.getCloneDepth());
        } catch (Exception e) {
            log.error("Checkpoint sync: Git sync failed, repoId={}, error={}", repoId, e.getMessage(), e);
            return;
        }

        // Fetch metadata branch to latest before walking content branches, read latest metadata from this branch when parsing checkpoints
        try {
            gitSyncService.fetchMetadataBranch(repoId, metadataBranch);
        } catch (Exception e) {
            log.error("Checkpoint sync: fetch metadata branch failed, repoId={}, branch={}, error={}", repoId, metadataBranch, e.getMessage(), e);
            return;
        }

        Optional<String> metadataRevOpt = gitReader.resolveBranchCommitSha(repoId, metadataBranch);
        if (metadataRevOpt.isEmpty()) {
            log.warn("Checkpoint sync: metadata branch has no commit or not found, repoId={}, branch={}", repoId, metadataBranch);
            return;
        }
        String metadataRevision = metadataRevOpt.get();

        List<String> contentBranches = gitReader.listLocalBranchNames(repoId).stream()
                .filter(b -> !metadataBranch.equals(b))
                .toList();
        if (contentBranches.isEmpty()) {
            log.info("Checkpoint sync: repo {} has no content branches (excluding {}), skip walk", repoId, metadataBranch);
            return;
        }

        // Incremental walk by branch: each branch uses its own lastProcessedCommitSha as stopAtSha
        List<CheckpointGitReader.CommitInfo> allCommits = new ArrayList<>();
        for (String branchName : contentBranches) {
            String stopAtSha = fullScan ? null : getLastProcessedCommitSha(repoId, branchName);
            log.debug("Checkpoint sync: processing branch {}, repoId={}", branchName, repoId);
            List<CheckpointGitReader.CommitInfo> commits = gitReader.walkCommitsFromBranch(repoId, branchName, stopAtSha);
            allCommits.addAll(commits);
            log.debug("Checkpoint sync: branch {} done, commits={}, repoId={}", branchName, commits.size(), repoId);
        }
        Collections.reverse(allCommits);

        for (CheckpointGitReader.CommitInfo commit : allCommits) {
            String checkpointId = parseTrailer(commit.fullMessage());
            if (checkpointId == null) {
                continue;
            }
            String path = checkpointId.substring(0, 2) + "/" + checkpointId.substring(2) + "/metadata.json";
            Optional<String> metaOpt = gitReader.getFileContent(repoId, metadataRevision, path);
            if (metaOpt.isEmpty()) {
                log.debug("Checkpoint metadata not found: repoId={}, commit={}, path={}", repoId, commit.commitSha(), path);
                continue;
            }
            String metaJson = metaOpt.get();
            CheckpointParseResult parsed = metadataParser.parseCheckpointMetadata(metaJson);
            if (parsed == null || parsed.getCheckpointId() == null) {
                continue;
            }
            String lastSessionPath = metadataParser.getLastSessionMetadataPath(metaJson);
            if (StrUtil.isNotBlank(lastSessionPath)) {
                Optional<String> sessionMeta = gitReader.getFileContent(repoId, metadataRevision, lastSessionPath);
                parsed.setAgent(sessionMeta.map(metadataParser::parseSessionAgent).orElse(null));
            }
            CheckpointGitReader.LineStats lineStats = gitReader.getCommitLineStats(repoId, commit.commitSha()).orElse(null);
            Checkpoint savedCheckpoint = upsertCheckpoint(repoId, commit.commitSha(), commit.fullMessage(), commit.authorName(), commit.commitTime(), parsed, lineStats);
            upsertSessionsForCheckpoint(repoId, metadataRevision, metaJson, savedCheckpoint.getId());
        }

        // Consistency: update state by branch only after all success (within transaction, any step failure rolls back entirely)
        long now = System.currentTimeMillis();
        for (String branchName : contentBranches) {
            String headSha = gitReader.resolveBranchCommitSha(repoId, branchName).orElse(null);
            if (headSha != null) {
                updateSyncStateSuccess(repoId, branchName, headSha, now);
            }
        }
        // Update repository table last successful sync time
        repo.setLastSuccessfulSyncAt(now);
        repoRepository.save(repo);
        log.info("Checkpoint sync done: repoId={}, contentBranches={}, processed={}", repoId, contentBranches.size(), allCommits.size());
    }

    @Override
    public void syncAllRepos(boolean fullScan) {
        List<Repo> repos = repoRepository.findAll();
        for (Repo repo : repos) {
            try {
                syncRepo(repo.getId(), fullScan);
            } catch (Exception e) {
                log.error("Checkpoint sync failed for repoId={}: {}", repo.getId(), e.getMessage(), e);
            }
        }
    }

    private boolean remoteHasBranch(Long repoId, String branch) {
        GitBranchesDTO dto = gitSyncService.getBranches(repoId);
        return dto.getBranches() != null && dto.getBranches().contains(branch);
    }

    private String parseTrailer(String fullMessage) {
        if (fullMessage == null) {
            return null;
        }
        Matcher m = TRAILER_PATTERN.matcher(fullMessage);
        String last = null;
        while (m.find()) {
            last = m.group(1);
        }
        return last;
    }

    private String getLastProcessedCommitSha(Long repoId, String branch) {
        return syncStateRepository.findByRepoIdAndBranch(repoId, branch)
                .map(CheckpointSyncState::getLastProcessedCommitSha)
                .orElse(null);
    }

    @Transactional
    protected Checkpoint upsertCheckpoint(Long repoId, String commitSha, String commitMessage, String commitAuthorName, long commitTime, CheckpointParseResult parsed, CheckpointGitReader.LineStats lineStats) {
        Checkpoint entity = checkpointRepository.findByRepoIdAndCheckpointId(repoId, parsed.getCheckpointId())
                .orElse(new Checkpoint());
        Repo repo = repoRepository.findById(repoId).orElse(null);
        entity.setCheckpointId(parsed.getCheckpointId());
        entity.setRepoId(repoId);
        entity.setRepoName(repo != null ? repo.getName() : null);
        entity.setBranch(parsed.getBranch());
        entity.setCommitSha(commitSha);
        entity.setCommitMessage(commitMessage);
        entity.setCommitAuthorName(commitAuthorName);
        entity.setCommitTime(commitTime);
        entity.setCheckpointsCount(parsed.getCheckpointsCount());
        entity.setFilesTouched(parsed.getFilesTouched());
        entity.setAdditions(lineStats != null ? lineStats.additions() : null);
        entity.setDeletions(lineStats != null ? lineStats.deletions() : null);
        entity.setTokenUsage(parsed.getTokenUsage());
        entity.setAgent(parsed.getAgent());
        return checkpointRepository.save(entity);
    }

    /// Upsert sessions for a checkpoint from metadata branch. Sessions array only (tasks/ excluded).
    private void upsertSessionsForCheckpoint(Long repoId, String metadataRevision, String checkpointMetaJson, Long checkpointId) {
        List<String> metadataPaths = metadataParser.getSessionMetadataPaths(checkpointMetaJson);
        for (int i = 0; i < metadataPaths.size(); i++) {
            String metaPath = metadataPaths.get(i);
            Optional<String> metaOpt = gitReader.getFileContent(repoId, metadataRevision, metaPath);
            if (metaOpt.isEmpty()) {
                log.debug("Session metadata not found: repoId={}, path={}", repoId, metaPath);
                continue;
            }
            SessionParseResult parsed = sessionMetadataParser.parse(metaOpt.get());
            if (parsed == null) {
                continue;
            }
            parsed.setSessionIndex(i);
            String promptPath = metadataParser.getSessionPromptPath(checkpointMetaJson, i);
            String promptPreview = null;
            if (StrUtil.isNotBlank(promptPath)) {
                Optional<String> promptOpt = gitReader.getFileContent(repoId, metadataRevision, promptPath);
                promptPreview = sessionMetadataParser.getFirstLineOfPrompt(promptOpt.orElse(null));
            }
            parsed.setPromptPreview(promptPreview);

            Session session = sessionRepository.findByCheckpointIdAndSessionIndex(checkpointId, i).orElse(new Session());
            session.setCheckpointId(checkpointId);
            session.setSessionIndex(i);
            copyParseResultToSession(parsed, session);
            sessionRepository.save(session);
        }
    }

    private void copyParseResultToSession(SessionParseResult from, Session to) {
        to.setSessionId(from.getSessionId());
        to.setStrategy(from.getStrategy());
        to.setSessionCreatedAt(from.getSessionCreatedAt());
        to.setBranch(from.getBranch());
        to.setCheckpointsCount(from.getCheckpointsCount());
        to.setFilesTouchedCount(from.getFilesTouchedCount());
        to.setFilesTouchedJson(from.getFilesTouchedJson());
        to.setAgent(from.getAgent());
        to.setInputTokens(from.getInputTokens());
        to.setOutputTokens(from.getOutputTokens());
        to.setApiCallCount(from.getApiCallCount());
        to.setAgentLines(from.getAgentLines());
        to.setHumanAdded(from.getHumanAdded());
        to.setHumanModified(from.getHumanModified());
        to.setHumanRemoved(from.getHumanRemoved());
        to.setTotalCommitted(from.getTotalCommitted());
        to.setAgentPercentage(from.getAgentPercentage());
        to.setPromptPreview(from.getPromptPreview());
    }

    private void updateSyncStateSuccess(Long repoId, String branch, String lastProcessedCommitSha, long now) {
        CheckpointSyncState state = syncStateRepository.findByRepoIdAndBranch(repoId, branch).orElseGet(() -> {
            CheckpointSyncState s = new CheckpointSyncState();
            s.setRepoId(repoId);
            s.setBranch(branch);
            s.setCreatedAt(now);
            return s;
        });
        state.setLastProcessedCommitSha(lastProcessedCommitSha);
        state.setLastSyncAt(now);
        state.setLastError(null);
        state.setUpdatedAt(now);
        syncStateRepository.save(state);
    }
}
