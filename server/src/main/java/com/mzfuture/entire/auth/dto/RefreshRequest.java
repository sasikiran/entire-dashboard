package com.mzfuture.entire.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// Refresh Token request DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    private String token;
}

