package com.mzfuture.entire.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/// Application configuration properties
@Component
@ConfigurationProperties(prefix = "app.user")
@Getter
@Setter
public class AppProperties {
    private String username = "admin";
    private String password = "admin";
}

