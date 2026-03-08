-- Create checkpoint table

CREATE TABLE `checkpoint`
(
    `id`                 BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    `checkpoint_id`      VARCHAR(12) NOT NULL COMMENT '12-digit hex checkpoint identifier',
    `repo_id`            BIGINT      NOT NULL COMMENT 'Repository ID',
    `repo_name`          VARCHAR(255) NULL COMMENT 'Repository name, redundant for listing',
    `branch`             VARCHAR(255) NULL COMMENT 'Git branch name',
    `commit_sha`         VARCHAR(64) NULL COMMENT 'Git commit SHA',
    `commit_time`        BIGINT      NOT NULL DEFAULT 0 COMMENT 'Git commit time (Unix epoch ms)',
    `commit_message`     VARCHAR(1024) NULL COMMENT 'Git commit message',
    `commit_author_name` VARCHAR(255) NULL COMMENT 'Git commit author display name',
    `checkpoints_count`  INTEGER NULL COMMENT 'Number of checkpoints in this run',
    `files_touched`      INTEGER NULL COMMENT 'Number of files touched',
    `additions`          INTEGER NULL COMMENT 'Lines added in this commit',
    `deletions`          INTEGER NULL COMMENT 'Lines deleted in this commit',
    `token_usage`        BIGINT NULL COMMENT 'Cumulative token usage',
    `agent`              VARCHAR(128) NULL COMMENT 'AI Agent name',
    `created_at`         BIGINT      NOT NULL COMMENT 'Creation timestamp (Unix epoch milliseconds)',
    `updated_at`         BIGINT      NOT NULL COMMENT 'Last update timestamp (Unix epoch milliseconds)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Checkpoint metadata for git commit and agent run';

CREATE INDEX `idx_checkpoint_commit_time` ON `checkpoint` (`commit_time`);
