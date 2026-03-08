package com.mzfuture.entire.checkpoint.repository;

import com.mzfuture.entire.checkpoint.dto.request.CheckpointFilterParams;
import org.springframework.stereotype.Repository;

import com.mzfuture.entire.checkpoint.dto.response.RepoOptionDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper for checkpoint filter dropdown queries (distinct repos, distinct commit authors).
 */
@Repository
public class CheckpointFilterRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<RepoOptionDTO> findDistinctRepos(CheckpointFilterParams params) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT new com.mzfuture.entire.checkpoint.dto.response.RepoOptionDTO(c.repoId, c.repoName) " +
                        "FROM Checkpoint c WHERE c.commitTime >= :startTime AND c.commitTime <= :endTime AND c.repoName IS NOT NULL ");
        Map<String, Object> bindParams = new HashMap<>();
        bindParams.put("startTime", params.getStartTime());
        bindParams.put("endTime", params.getEndTime());
        if (StringUtils.hasText(params.getCommitMessage())) {
            jpql.append(" AND LOWER(c.commitMessage) LIKE LOWER(CONCAT('%', :commitMessage, '%')) ");
            bindParams.put("commitMessage", params.getCommitMessage());
        }
        if (params.getCommitAuthorNames() != null && !params.getCommitAuthorNames().isEmpty()) {
            jpql.append(" AND c.commitAuthorName IN :commitAuthorNames ");
            bindParams.put("commitAuthorNames", params.getCommitAuthorNames());
        }
        jpql.append(" ORDER BY c.repoName");
        TypedQuery<RepoOptionDTO> query = entityManager.createQuery(jpql.toString(), RepoOptionDTO.class);
        bindParams.forEach(query::setParameter);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> findDistinctCommitAuthors(CheckpointFilterParams params) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT c.commitAuthorName FROM Checkpoint c " +
                        "WHERE c.commitTime >= :startTime AND c.commitTime <= :endTime " +
                        "AND c.commitAuthorName IS NOT NULL ");
        Map<String, Object> bindParams = new HashMap<>();
        bindParams.put("startTime", params.getStartTime());
        bindParams.put("endTime", params.getEndTime());
        if (params.getRepoIds() != null && !params.getRepoIds().isEmpty()) {
            jpql.append(" AND c.repoId IN :repoIds ");
            bindParams.put("repoIds", params.getRepoIds());
        }
        if (StringUtils.hasText(params.getCommitMessage())) {
            jpql.append(" AND LOWER(c.commitMessage) LIKE LOWER(CONCAT('%', :commitMessage, '%')) ");
            bindParams.put("commitMessage", params.getCommitMessage());
        }
        jpql.append(" ORDER BY c.commitAuthorName");
        TypedQuery<String> query = entityManager.createQuery(jpql.toString(), String.class);
        bindParams.forEach(query::setParameter);
        return query.getResultList();
    }
}
