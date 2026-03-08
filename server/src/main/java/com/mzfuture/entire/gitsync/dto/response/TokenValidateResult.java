package com.mzfuture.entire.gitsync.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/// Token validation result
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidateResult {

    @Schema(description = "Whether the token is valid")
    private boolean valid;

    @Schema(description = "Message (e.g. error reason when invalid)")
    private String message;

    public static TokenValidateResult ok() {
        return TokenValidateResult.builder().valid(true).build();
    }

    public static TokenValidateResult fail(String message) {
        return TokenValidateResult.builder().valid(false).message(message).build();
    }
}
