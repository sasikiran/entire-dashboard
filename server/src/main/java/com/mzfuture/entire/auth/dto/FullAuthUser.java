package com.mzfuture.entire.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// Login response DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullAuthUser {
    private String username;
    private String accessToken;
    private long expire;  // Access Token expiration time (millisecond timestamp)
    private String refreshToken;
    private long refreshExpire;  // Refresh Token expiration time (millisecond timestamp)
}

