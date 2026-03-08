package com.mzfuture.entire.auth.controller;

import com.mzfuture.entire.auth.dto.FullAuthUser;
import com.mzfuture.entire.auth.dto.LoginRequest;
import com.mzfuture.entire.auth.dto.RefreshRequest;
import com.mzfuture.entire.auth.service.AuthService;
import com.mzfuture.entire.common.dto.StatusResult;
import com.mzfuture.entire.common.exception.Errors;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/// Authentication controller
@RestController
@RequestMapping("/api/v1/open/auth")
public class AuthController {
    private static final String REFRESH_TOKEN_COOKIE_KEY = "refreshToken";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /// Login
    @PostMapping("/login")
    public FullAuthUser login(@RequestBody LoginRequest request, HttpServletResponse response) {
        FullAuthUser fullAuthUser = authService.login(request);

        // Write Refresh Token to HttpOnly Cookie
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE_KEY, fullAuthUser.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((30 * 24 * 60 * 60)); // 30 days
        refreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshCookie);

        return fullAuthUser;
    }

    /// Refresh token
    @PostMapping("/refresh")
    public FullAuthUser refresh(@RequestBody(required = false) RefreshRequest params,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        // Prefer to get refresh token from Cookie
        String refreshToken = extractTokenFromCookie(request)
                .orElseGet(() -> params != null ? params.getToken() : null);

        if (!StringUtils.hasText(refreshToken)) {
            throw Errors.UNAUTHORIZED.toException("Please provide refresh token");
        }

        FullAuthUser fullAuthUser = authService.refresh(refreshToken);

        // Update Refresh Token Cookie
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE_KEY, fullAuthUser.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (30 * 24 * 60 * 60)); // 30 days
        refreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshCookie);

        return fullAuthUser;
    }

    /// Logout
    @PostMapping("/logout")
    public StatusResult<Void> logout(HttpServletResponse response) {
        // Clear Refresh Token Cookie
        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE_KEY, "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // Expire immediately
        response.addCookie(refreshCookie);

        return StatusResult.ok();
    }

    /// Extract refresh token from Cookie
    private Optional<String> extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (REFRESH_TOKEN_COOKIE_KEY.equals(cookie.getName())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }
}

