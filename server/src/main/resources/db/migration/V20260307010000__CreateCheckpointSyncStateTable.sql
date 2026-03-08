-- Create checkpoint_sync_state table for incremental checkpoint sync per repo

CREATE TABLE `checkpoint_sync_state`
(
    `repo_id`                   BIGINT       NOT NULL COMMENT 'Repository ID (FK to repository.id)',
    `branch`                    VARCHAR(255) NOT NULL COMMENT 'Branch name (PK)',
    `last_processed_commit_sha` VARCHAR(64) NULL COMMENT 'Last commit SHA already processed for incremental sync',
    `last_sync_at`              BIGINT NULL COMMENT 'Last successful sync finish time (Unix epoch milliseconds)',
    `last_error`                VARCHAR(512) NULL COMMENT 'Last sync error message if failed',
    `created_at`                BIGINT       NOT NULL COMMENT 'Creation timestamp (Unix epoch milliseconds)',
    `updated_at`                BIGINT       NOT NULL COMMENT 'Last update timestamp (Unix epoch milliseconds)',

    PRIMARY KEY (`repo_id`, `branch`),

    CONSTRAINT `fk_checkpoint_sync_state_repo` FOREIGN KEY (`repo_id`) REFERENCES `repository` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Checkpoint sync state per repository for incremental commit walk';
