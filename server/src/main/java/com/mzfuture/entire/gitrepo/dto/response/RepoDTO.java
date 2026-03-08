package com.mzfuture.entire.gitrepo.dto.response;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RepoDTO {
    @Schema(description = "Repository ID", example = "1")
    private Long id;
    @Schema(description = "Repository name", example = "My Repository")
    private String name;
    @Schema(description = "Repository description", example = "This is a test repository")
    private String webUrl;
    @Schema(description = "Code repository platform", example = "GITHUB")
    private RepositoryPlatform platform;
    @Schema(description = "Access token", example = "ghp_xxxxxxxxxxxx")
    private String accessToken;
    @Schema(description = "Creation timestamp", example = "1704067200000")
    private Long createdAt;
    @Schema(description = "Update timestamp", example = "1704067200000")
    private Long updatedAt;
    @Schema(description = "Last successful checkpoint sync timestamp", example = "1704067200000")
    private Long lastSuccessfulSyncAt;
}
