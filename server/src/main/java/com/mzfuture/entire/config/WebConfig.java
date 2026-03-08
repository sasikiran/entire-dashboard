package com.mzfuture.entire.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/// Web configuration (CORS)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /// This Bean is preferred when using Spring Security or global CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Use wildcard pattern to allow any domain
        configuration.addAllowedOriginPattern("*");

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allowed all headers
        configuration.addAllowedHeader("*");

        // Cache preflight request for 1 hour
        configuration.setMaxAge(3600L);

        // Configure path
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
