package com.mzfuture.entire.checkpoint.repository;

import com.mzfuture.entire.checkpoint.entity.Checkpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CheckpointRepository extends JpaRepository<Checkpoint, Long>, QuerydslPredicateExecutor<Checkpoint> {

    /// Find checkpoint by 12-digit hex checkpoint id
    ///
    /// @param checkpointId checkpoint identifier
    /// @return checkpoint optional
    Optional<Checkpoint> findByCheckpointId(String checkpointId);

    boolean existsByCheckpointId(String checkpointId);

    /// Find by repo and 12-digit hex checkpoint id (for upsert)
    Optional<Checkpoint> findByRepoIdAndCheckpointId(Long repoId, String checkpointId);

    /// Count distinct repo ids with checkpoints in time range (active projects, by commitTime)
    @Query("SELECT COUNT(DISTINCT c.repoId) FROM Checkpoint c WHERE c.commitTime >= :start AND c.commitTime <= :end")
    Long countDistinctRepoIdsByCommitTimeBetween(@Param("start") Long start, @Param("end") Long end);

    /// Count distinct commit authors in time range (excludes null commitAuthorName, by commitTime)
    @Query("SELECT COUNT(DISTINCT c.commitAuthorName) FROM Checkpoint c WHERE c.commitTime >= :start AND c.commitTime <= :end AND c.commitAuthorName IS NOT NULL")
    Long countDistinctCommitAuthorsByCommitTimeBetween(@Param("start") Long start, @Param("end") Long end);

    /// Count checkpoints in time range (by commitTime)
    @Query("SELECT COUNT(c) FROM Checkpoint c WHERE c.commitTime >= :start AND c.commitTime <= :end")
    Long countByCommitTimeBetween(@Param("start") Long start, @Param("end") Long end);

    /// Sum token usage in time range (null treated as 0, by commitTime)
    @Query("SELECT COALESCE(SUM(c.tokenUsage), 0) FROM Checkpoint c WHERE c.commitTime >= :start AND c.commitTime <= :end")
    Long sumTokenUsageByCommitTimeBetween(@Param("start") Long start, @Param("end") Long end);
}
