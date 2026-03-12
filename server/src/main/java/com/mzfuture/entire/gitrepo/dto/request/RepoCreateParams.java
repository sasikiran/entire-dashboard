package com.mzfuture.entire.gitrepo.dto.request;

import com.mzfuture.entire.gitrepo.enums.RepositoryPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Repository Creation Request Parameters")
public class RepoCreateParams {
    @NotBlank(message = "Repository name cannot be empty")
    @Size(max = 255, message = "Repository name length cannot exceed 255 characters")
    @Schema(description = "Repository name", requiredMode = Schema.RequiredMode.REQUIRED, example = "my-repository")
    private String name;

    @NotBlank(message = "Repository URL or local path cannot be empty")
    @Schema(description = "Repository web URL or local path", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://github.com/user/repo")
    private String webUrl;

    @NotNull(message = "Platform cannot be null")
    @Schema(description = "Code repository platform", requiredMode = Schema.RequiredMode.REQUIRED, example = "GITHUB")
    private RepositoryPlatform platform;

    @Schema(description = "Access token for authentication", example = "ghp_xxxxxxxxxxxx")
    private String accessToken;
}
