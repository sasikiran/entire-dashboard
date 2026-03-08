package com.mzfuture.entire.checkpoint.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mzfuture.entire.checkpoint.dto.request.CheckpointFilterParams;
import com.mzfuture.entire.checkpoint.dto.response.CheckpointDTO;
import com.mzfuture.entire.checkpoint.dto.request.CheckpointSearchParams;
import com.mzfuture.entire.checkpoint.dto.response.RepoOptionDTO;
import com.mzfuture.entire.checkpoint.dto.request.CheckpointUpdateParams;
import com.mzfuture.entire.checkpoint.entity.Checkpoint;
import com.mzfuture.entire.checkpoint.entity.QCheckpoint;
import com.mzfuture.entire.checkpoint.mapper.CheckpointMapper;
import com.mzfuture.entire.checkpoint.repository.CheckpointFilterRepository;
import com.mzfuture.entire.checkpoint.repository.CheckpointRepository;
import com.mzfuture.entire.checkpoint.service.CheckpointService;
import com.mzfuture.entire.common.exception.Errors;
import com.mzfuture.entire.gitrepo.entity.Repo;
import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import com.mzfuture.entire.gitrepo.repository.RepoRepository;
import com.mzfuture.entire.gitsync.adapter.GitPlatformAdapter;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
public class CheckpointServiceImpl implements CheckpointService {

    private final CheckpointRepository checkpointRepository;
    private final CheckpointFilterRepository filterRepository;
    private final CheckpointMapper checkpointMapper;
    private final RepoRepository repoRepository;
    private final Map<RepositoryPlatform, GitPlatformAdapter> platformAdapters;

    public CheckpointServiceImpl(CheckpointRepository checkpointRepository,
                                 CheckpointFilterRepository filterRepository,
                                 CheckpointMapper checkpointMapper,
                                 RepoRepository repoRepository,
                                 List<GitPlatformAdapter> adapters) {
        this.checkpointRepository = checkpointRepository;
        this.filterRepository = filterRepository;
        this.checkpointMapper = checkpointMapper;
        this.repoRepository = repoRepository;
        this.platformAdapters = adapters.stream()
                .collect(Collectors.toMap(GitPlatformAdapter::getPlatform, a -> a));
    }

    @Override
    public CheckpointDTO update(CheckpointUpdateParams params) {
        Checkpoint entity = checkpointRepository.findById(params.getId())
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Checkpoint not found, id: " + params.getId()));
        checkpointMapper.updateEntity(params, entity);
        Checkpoint saved = checkpointRepository.save(entity);
        log.info("Updated checkpoint successfully, ID: {}, checkpointId: {}", saved.getId(), saved.getCheckpointId());
        CheckpointDTO dto = checkpointMapper.toDTO(saved);
        enrichCommitUrl(dto);
        return dto;
    }

    @Override
    public CheckpointDTO get(Long id) {
        Checkpoint entity = checkpointRepository.findById(id)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Checkpoint not found, ID: " + id));
        CheckpointDTO dto = checkpointMapper.toDTO(entity);
        enrichCommitUrl(dto);
        return dto;
    }

    @Override
    public void delete(Long id) {
        if (!checkpointRepository.existsById(id)) {
            throw Errors.INTERNAL_ERROR.toException("Checkpoint not found, ID: " + id);
        }
        checkpointRepository.deleteById(id);
        log.info("Deleted checkpoint successfully, ID: {}", id);
    }

    @Override
    public Page<CheckpointDTO> search(CheckpointSearchParams params, Pageable pageable) {
        QCheckpoint q = QCheckpoint.checkpoint;
        BooleanBuilder b = new BooleanBuilder();
        if (params.getRepoIds() != null && !params.getRepoIds().isEmpty()) {
            b.and(q.repoId.in(params.getRepoIds()));
        }
        if (params.getCommitAuthorNames() != null && !params.getCommitAuthorNames().isEmpty()) {
            b.and(q.commitAuthorName.in(params.getCommitAuthorNames()));
        }
        if (StrUtil.isNotBlank(params.getBranch())) {
            b.and(q.branch.containsIgnoreCase(params.getBranch()));
        }
        if (StrUtil.isNotBlank(params.getCommitMessage())) {
            b.and(q.commitMessage.containsIgnoreCase(params.getCommitMessage()));
        }
        if (params.getStartTime() != null) {
            b.and(q.commitTime.goe(params.getStartTime()));
        }
        if (params.getEndTime() != null) {
            b.and(q.commitTime.loe(params.getEndTime()));
        }
        var page = checkpointRepository.findAll(b, pageable);
        var rows = checkpointMapper.toRows(page.toList());
        enrichCommitUrls(rows);
        return new PageImpl<>(rows, pageable, page.getTotalElements());
    }

    private void enrichCommitUrl(CheckpointDTO dto) {
        if (dto.getRepoId() == null || StrUtil.isBlank(dto.getCommitSha())) {
            return;
        }
        repoRepository.findById(dto.getRepoId()).ifPresent(repo -> {
            GitPlatformAdapter adapter = platformAdapters.get(repo.getPlatform());
            if (adapter != null) {
                String url = adapter.buildCommitUrl(repo.getWebUrl(), dto.getCommitSha());
                if (url != null) {
                    dto.setCommitUrl(url);
                }
            }
        });
    }

    private void enrichCommitUrls(List<CheckpointDTO> dtos) {
        Set<Long> repoIds = dtos.stream()
                .map(CheckpointDTO::getRepoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (repoIds.isEmpty()) {
            return;
        }
        Map<Long, Repo> repoMap = repoRepository.findAllById(repoIds).stream()
                .collect(Collectors.toMap(Repo::getId, r -> r));
        for (CheckpointDTO dto : dtos) {
            if (dto.getRepoId() != null && StrUtil.isNotBlank(dto.getCommitSha())) {
                Repo repo = repoMap.get(dto.getRepoId());
                if (repo != null) {
                    GitPlatformAdapter adapter = platformAdapters.get(repo.getPlatform());
                    if (adapter != null) {
                        String url = adapter.buildCommitUrl(repo.getWebUrl(), dto.getCommitSha());
                        if (url != null) {
                            dto.setCommitUrl(url);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<RepoOptionDTO> getReposForFilter(CheckpointFilterParams params) {
        return filterRepository.findDistinctRepos(params);
    }

    @Override
    public List<String> getCommitAuthorsForFilter(CheckpointFilterParams params) {
        return filterRepository.findDistinctCommitAuthors(params);
    }
}
