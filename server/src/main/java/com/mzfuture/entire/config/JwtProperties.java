package com.mzfuture.entire.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/// JWT configuration properties
@Component
@ConfigurationProperties(prefix = "auth.jwt")
@Getter
@Setter
public class JwtProperties {
    private String issuer = "entire-dashboard";
    private String secret = "ChangeThisSecretKeyInProduction";
    private long accessTokenTtl = 1800000L;      // 30 minutes
    private long refreshTokenTtl = 2592000000L;  // 30 days
}

