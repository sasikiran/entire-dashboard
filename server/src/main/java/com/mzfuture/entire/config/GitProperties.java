package com.mzfuture.entire.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/// Git同步配置属性
@Component
@ConfigurationProperties(prefix = "app.git")
@Getter
@Setter
public class GitProperties {

    /// 代码存储根目录，默认为./data
    private String dataPath = "./data";

    /// 默认同步分支，默认为entire/checkpoints/v1
    private String defaultBranch = "entire/checkpoints/v1";

    /// 浅克隆深度，默认为1（仅最新commit）
    private int cloneDepth = 1;

    /// Git操作超时时间（秒），默认为1800秒
    private int timeoutSeconds = 1800;

    /// 操作失败时重试次数，默认为3次
    private int retryCount = 3;
}
