package com.mzfuture.entire.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IdPayload {
    @Schema(description = "ID", type = "string", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private Long id;
}
