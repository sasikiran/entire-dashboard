-- Create session table (one checkpoint has many sessions; tasks/ sub-agent excluded this phase)

CREATE TABLE `session`
(
    `id`                  BIGINT  NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    `checkpoint_id`       BIGINT  NOT NULL COMMENT 'Reference to checkpoint.id',
    `session_id`          VARCHAR(64) NULL COMMENT 'Session identifier for reference (e.g. ses_xxx)',
    `session_index`       INTEGER NOT NULL COMMENT '0-based index within checkpoint',
    `strategy`            VARCHAR(64) NULL COMMENT 'Strategy e.g. manual-commit',
    `session_created_at`  BIGINT NULL COMMENT 'Session creation time (Unix epoch ms)',
    `branch`              VARCHAR(255) NULL COMMENT 'Git branch name',
    `checkpoints_count`   INTEGER NULL COMMENT 'Number of checkpoints in run',
    `files_touched_count` INTEGER NULL COMMENT 'Count of files touched',
    `files_touched_json`  TEXT NULL COMMENT 'JSON array of touched file paths',
    `agent`               VARCHAR(128) NULL COMMENT 'AI Agent name',
    `input_tokens`        BIGINT NULL COMMENT 'Token usage input',
    `output_tokens`       BIGINT NULL COMMENT 'Token usage output',
    `api_call_count`      INTEGER NULL COMMENT 'API call count',
    `agent_lines`         INTEGER NULL COMMENT 'Attribution: agent lines',
    `human_added`         INTEGER NULL COMMENT 'Attribution: human added',
    `human_modified`      INTEGER NULL COMMENT 'Attribution: human modified',
    `human_removed`       INTEGER NULL COMMENT 'Attribution: human removed',
    `total_committed`     INTEGER NULL COMMENT 'Attribution: total committed',
    `agent_percentage`    DECIMAL(5, 2) NULL COMMENT 'Attribution: agent percentage',
    `prompt_preview`      VARCHAR(512) NULL COMMENT 'First line of prompt.txt for list preview',
    `created_at`          BIGINT  NOT NULL COMMENT 'Creation timestamp (Unix epoch ms)',
    `updated_at`          BIGINT  NOT NULL COMMENT 'Last update timestamp (Unix epoch ms)',
    UNIQUE KEY `uk_checkpoint_session_index` (`checkpoint_id`, `session_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Session metadata per checkpoint (tasks/ excluded)';
