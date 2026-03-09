package com.mzfuture.entire.checkpoint.job;

import com.mzfuture.entire.checkpoint.config.CheckpointSyncProperties;
import com.mzfuture.entire.checkpoint.service.CheckpointSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/// Scheduled job: sync checkpoints for all repos (incremental commit walk).
/// Runs once at startup (ApplicationReadyEvent) and then on cron schedule.
@Slf4j
@Component
public class CheckpointSyncJob {

    private final CheckpointSyncProperties syncProperties;
    private final CheckpointSyncService checkpointSyncService;

    public CheckpointSyncJob(CheckpointSyncProperties syncProperties,
                             CheckpointSyncService checkpointSyncService) {
        this.syncProperties = syncProperties;
        this.checkpointSyncService = checkpointSyncService;
    }

    /** Run once when application is ready (after context and beans are initialized). */
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        CompletableFuture.runAsync(this::run);
    }

    @Scheduled(cron = "${app.checkpoint.sync.cron:0 */15 * * * ?}")
    public void run() {
        if (!syncProperties.isEnabled()) {
            log.debug("Checkpoint sync job disabled, skip");
            return;
        }
        log.info("Checkpoint sync job start");
        checkpointSyncService.syncAllRepos(false);
        log.info("Checkpoint sync job finish");
    }
}
