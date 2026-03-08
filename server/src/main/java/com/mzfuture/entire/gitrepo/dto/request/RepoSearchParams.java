package com.mzfuture.entire.gitrepo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Repository search request parameters")
public class RepoSearchParams {

    @Schema(description = "Search keyword (fuzzy search on repository name and description)", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "test")
    private String keyword;
}

