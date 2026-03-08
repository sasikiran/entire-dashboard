package com.mzfuture.entire.gitsync.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/// Repository branch list information
@Data
@Schema(description = "Repository branch list information")
public class GitBranchesDTO {

    /// Repository ID
    @Schema(description = "Repository ID")
    private Long repoId;

    /// Branch name list
    @Schema(description = "Branch name list")
    private List<String> branches;
}
