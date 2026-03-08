package com.mzfuture.entire.checkpoint.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Repository option for checkpoint filter dropdown")
public class RepoOptionDTO {

    @Schema(description = "Repository ID")
    private Long repoId;

    @Schema(description = "Repository name")
    private String repoName;
}
