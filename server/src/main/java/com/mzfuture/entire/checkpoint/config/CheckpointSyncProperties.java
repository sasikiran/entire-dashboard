package com.mzfuture.entire.checkpoint.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/// Checkpoint sync job and Git clone options for entire/checkpoints/v1 branch.
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.checkpoint.sync")
public class CheckpointSyncProperties {

    /// Whether scheduled checkpoint sync is enabled
    private boolean enabled = true;

    /// Cron expression for scheduled sync (e.g. "0 */15 * * * ?" every 15 min)
    private String cron = "0 */15 * * * ?";

    /// 仅存 checkpoint 元数据的分支名，不参与 commit 遍历；遍历的是除本分支外的所有其他分支 (default entire/checkpoints/v1)
    private String branch = "entire/checkpoints/v1";

    /// Clone depth for this branch only; 0 = full history, >0 = shallow (e.g. 500)
    private int cloneDepth = 0;
}
