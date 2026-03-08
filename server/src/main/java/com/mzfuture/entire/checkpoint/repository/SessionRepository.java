package com.mzfuture.entire.checkpoint.repository;

import com.mzfuture.entire.checkpoint.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    /// Find all sessions for a checkpoint, ordered by session_index
    List<Session> findByCheckpointIdOrderBySessionIndexAsc(Long checkpointId);

    /// Find by checkpoint and session index (for upsert)
    Optional<Session> findByCheckpointIdAndSessionIndex(Long checkpointId, Integer sessionIndex);
}
