-- Create repository table

CREATE TABLE `repository`
(
    `id`                      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key ID',
    `name`                    VARCHAR(255) COMMENT 'Repository name',
    `web_url`                 VARCHAR(255) COMMENT 'Repository web URL',
    `platform`                VARCHAR(64) COMMENT 'Source control platform (e.g., GitHub, GitLab)',
    `access_token`            VARCHAR(255) COMMENT 'Access token for API authentication',
    `last_successful_sync_at` BIGINT NULL COMMENT 'Last successful checkpoint sync time (Unix epoch milliseconds). NULL if never synced.',
    `created_at`              BIGINT NOT NULL COMMENT 'Creation timestamp (Unix epoch milliseconds)',
    `updated_at`              BIGINT NOT NULL COMMENT 'Last update timestamp (Unix epoch milliseconds)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Repository metadata table';
