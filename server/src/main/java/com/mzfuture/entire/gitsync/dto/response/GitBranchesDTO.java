package com.mzfuture.entire.gitsync.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/// 仓库分支列表信息
@Data
@Schema(description = "仓库分支列表信息")
public class GitBranchesDTO {

    /// 仓库ID
    @Schema(description = "仓库ID")
    private Long repoId;

    /// 分支名称列表
    @Schema(description = "分支名称列表")
    private List<String> branches;
}
