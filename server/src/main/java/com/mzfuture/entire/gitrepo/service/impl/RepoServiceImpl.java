package com.mzfuture.entire.gitrepo.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mzfuture.entire.gitrepo.dto.request.RepoCreateParams;
import com.mzfuture.entire.gitrepo.dto.response.RepoDTO;
import com.mzfuture.entire.gitrepo.dto.request.RepoSearchParams;
import com.mzfuture.entire.gitrepo.dto.request.RepoUpdateParams;
import com.mzfuture.entire.gitrepo.entity.QRepo;
import com.mzfuture.entire.gitrepo.entity.Repo;
import com.mzfuture.entire.gitrepo.mapper.RepoMapper;
import com.mzfuture.entire.gitrepo.repository.RepoRepository;
import com.mzfuture.entire.gitrepo.service.RepoService;
import com.mzfuture.entire.gitsync.service.GitOperationService;
import com.mzfuture.entire.common.exception.Errors;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class RepoServiceImpl implements RepoService {

    private final RepoRepository repoRepository;
    private final RepoMapper repoMapper;
    private final GitOperationService gitOperationService;

    public RepoServiceImpl(RepoRepository repoRepository,
                          RepoMapper repoMapper,
                          GitOperationService gitOperationService) {
        this.repoRepository = repoRepository;
        this.repoMapper = repoMapper;
        this.gitOperationService = gitOperationService;
    }

@Override
    public RepoDTO create(RepoCreateParams params) {
        if (repoRepository.existsByName(params.getName())) {
            throw Errors.INTERNAL_ERROR.toException("Repository name already exists: " + params.getName());
        }
        if (repoRepository.existsByWebUrl(params.getWebUrl())) {
            throw Errors.INTERNAL_ERROR.toException("Repository Web URL already exists: " + params.getWebUrl());
        }
        Repo repo = repoMapper.toEntity(params);
        Repo saved = repoRepository.save(repo);
        return repoMapper.toDTO(saved);
    }

    @Override
    public RepoDTO update(RepoUpdateParams params) {
        Repo repo = repoRepository.findById(params.getId())
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Repository not found, id: " + params.getId()));

        if (repoRepository.existsByNameAndIdNot(params.getName(), params.getId())) {
            throw Errors.INTERNAL_ERROR.toException("Repository name already exists: " + params.getName());
        }
        repoMapper.updateEntity(params, repo);

        Repo saved = repoRepository.save(repo);
        log.info("Updated repository successfully, ID: {}, Name: {}", saved.getId(), saved.getName());

        return repoMapper.toDTO(saved);
    }

    @Override
    public RepoDTO get(Long id) {
        Repo repo = repoRepository.findById(id)
                .orElseThrow(() -> Errors.NOT_FOUND.toException("Repository not found, ID: " + id));

        return repoMapper.toDTO(repo);
    }

    @Override
    public void delete(Long id) {
        if (!repoRepository.existsById(id)) {
            throw Errors.INTERNAL_ERROR.toException("Repository not found, ID: " + id);
        }

        // Delete local code repository
        try {
            boolean deleted = gitOperationService.deleteRepository(id);
            if (deleted) {
                log.info("Deleted local repository successfully, ID: {}", id);
            }
        } catch (Exception e) {
            log.warn("Failed to delete local repository, ID: {}, error: {}", id, e.getMessage());
        }

        repoRepository.deleteById(id);
        log.info("Deleted repository record successfully, ID: {}", id);
    }

    @Override
    public Page<RepoDTO> search(RepoSearchParams params, Pageable pageable) {
        QRepo qRepo = QRepo.repo;

        BooleanBuilder b = new BooleanBuilder();
        String keyword = params.getKeyword();
        if (StrUtil.isNotBlank(keyword)) {
            b.and(
                    qRepo.name.containsIgnoreCase(keyword)
                            .or(qRepo.webUrl.containsIgnoreCase(keyword))
            );
        }

        var repos = repoRepository.findAll(b, pageable);
        var rows = repoMapper.toRows(repos.toList());
        return new PageImpl<>(rows, pageable, repos.getTotalElements());
    }
}
