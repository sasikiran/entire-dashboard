package com.mzfuture.entire.auth.filter;

import com.mzfuture.entire.auth.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/// JWT authentication filter
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String COOKIE_KEY = "accessToken";

    private final AuthService authService;

    public JwtAuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractJwtFromRequest(request);
            if (StringUtils.hasText(token)) {
                String username = authService.validateAccessToken(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Set authentication context
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
            // Continue execution, let Spring Security handle unauthenticated requests
        }

        filterChain.doFilter(request, response);
    }

    /// Extract JWT token from request
    /// Prefer to get from Cookie, then from Header
    private String extractJwtFromRequest(HttpServletRequest request) {
        // First try to get accessToken from Cookie
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (COOKIE_KEY.equals(cookie.getName())) {
                    logger.debug("Got token from HTTP Cookie");
                    return cookie.getValue();
                }
            }
        }

        // If not found in Cookie, get from Header
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            logger.debug("Got token from HTTP Header");
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}

