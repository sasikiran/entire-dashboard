package com.mzfuture.entire.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/// Git sync configuration properties
@Component
@ConfigurationProperties(prefix = "app.git")
@Getter
@Setter
public class GitProperties {

    /// Root directory for code storage, default is ./data
    private String dataPath = "./data";

    /// Default sync branch, default is entire/checkpoints/v1
    private String defaultBranch = "entire/checkpoints/v1";

    /// Shallow clone depth, default is 1 (latest commit only)
    private int cloneDepth = 1;

    /// Git operation timeout in seconds, default is 1800 seconds
    private int timeoutSeconds = 1800;

    /// Number of retries on operation failure, default is 3
    private int retryCount = 3;
}
