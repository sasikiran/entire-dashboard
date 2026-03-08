package com.mzfuture.entire.gitsync.controller;

import com.mzfuture.entire.gitsync.dto.request.TokenValidateParams;
import com.mzfuture.entire.gitsync.dto.response.GitBranchesDTO;
import com.mzfuture.entire.gitsync.dto.response.GitStatusDTO;
import com.mzfuture.entire.gitsync.dto.request.GitSyncParams;
import com.mzfuture.entire.gitsync.dto.response.GitSyncResult;
import com.mzfuture.entire.gitsync.dto.response.TokenValidateResult;
import com.mzfuture.entire.gitsync.service.GitOperationService;
import com.mzfuture.entire.gitsync.service.GitSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/// Git仓库同步控制器
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/repo")
@Tag(name = "Repository Sync", description = "仓库代码同步API")
public class GitSyncController {

    private final GitSyncService gitSyncService;
    private final GitOperationService gitOperationService;

    /// 同步仓库代码（根据webUrl和accessToken）
    ///
    /// @param params 同步参数
    /// @return 同步结果
    @PostMapping("/sync")
    @Operation(summary = "同步仓库代码", description = "根据webUrl和accessToken同步指定分支代码到本地")
    public GitSyncResult sync(@RequestBody @Valid GitSyncParams params) {
        return gitSyncService.syncRepository(params);
    }

    /// 根据仓库ID同步（使用存储的配置）
    ///
    /// @param repoId 仓库ID
    /// @return 同步结果
    @PostMapping("/sync/{repoId}")
    @Operation(summary = "根据ID同步仓库", description = "使用数据库中存储的仓库配置进行同步")
    public GitSyncResult syncById(@PathVariable Long repoId) {
        return gitSyncService.syncRepositoryById(repoId);
    }

    /// 获取仓库同步状态
    ///
    /// @param repoId 仓库ID
    /// @return 仓库状态信息
    @GetMapping("/sync/status/{repoId}")
    @Operation(summary = "获取仓库同步状态", description = "检查本地仓库是否存在及当前状态")
    public GitStatusDTO getSyncStatus(@PathVariable Long repoId) {
        return gitOperationService.getRepositoryStatus(repoId);
    }

    /// 获取仓库所有分支列表
    ///
    /// @param repoId 仓库ID
    /// @return 分支列表信息
    @GetMapping("/branches/{repoId}")
    @Operation(summary = "获取仓库分支列表", description = "获取指定仓库的所有远程分支名称列表")
    public GitBranchesDTO getBranches(@PathVariable Long repoId) {
        return gitSyncService.getBranches(repoId);
    }

    /// 校验访问令牌有效性（调用各平台 API）
    ///
    /// @param params webUrl、platform、accessToken
    /// @return 校验结果
    @PostMapping("/validate-token")
    @Operation(summary = "校验 Token", description = "调用平台 API 校验访问令牌是否有效")
    public TokenValidateResult validateToken(@RequestBody @Valid TokenValidateParams params) {
        return gitSyncService.validateToken(params);
    }
}
