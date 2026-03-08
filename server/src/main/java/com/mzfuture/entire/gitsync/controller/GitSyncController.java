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

/// Git repository sync controller
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/repo")
@Tag(name = "Repository Sync", description = "Repository code sync API")
public class GitSyncController {

    private final GitSyncService gitSyncService;
    private final GitOperationService gitOperationService;

    /// Sync repository code (based on webUrl and accessToken)
    ///
    /// @param params sync parameters
    /// @return sync result
    @PostMapping("/sync")
    @Operation(summary = "Sync repository code", description = "Sync specified branch code to local based on webUrl and accessToken")
    public GitSyncResult sync(@RequestBody @Valid GitSyncParams params) {
        return gitSyncService.syncRepository(params);
    }

    /// Sync by repository ID (using stored configuration)
    ///
    /// @param repoId repository ID
    /// @return sync result
    @PostMapping("/sync/{repoId}")
    @Operation(summary = "Sync repository by ID", description = "Sync using repository configuration stored in database")
    public GitSyncResult syncById(@PathVariable Long repoId) {
        return gitSyncService.syncRepositoryById(repoId);
    }

    /// Get repository sync status
    ///
    /// @param repoId repository ID
    /// @return repository status information
    @GetMapping("/sync/status/{repoId}")
    @Operation(summary = "Get repository sync status", description = "Check if local repository exists and current status")
    public GitStatusDTO getSyncStatus(@PathVariable Long repoId) {
        return gitOperationService.getRepositoryStatus(repoId);
    }

    /// Get all branches of repository
    ///
    /// @param repoId repository ID
    /// @return branch list information
    @GetMapping("/branches/{repoId}")
    @Operation(summary = "Get repository branches", description = "Get all remote branch names for specified repository")
    public GitBranchesDTO getBranches(@PathVariable Long repoId) {
        return gitSyncService.getBranches(repoId);
    }

    /// Validate access token validity (calls platform API)
    ///
    /// @param params webUrl, platform, accessToken
    /// @return validation result
    @PostMapping("/validate-token")
    @Operation(summary = "Validate Token", description = "Call platform API to validate if access token is valid")
    public TokenValidateResult validateToken(@RequestBody @Valid TokenValidateParams params) {
        return gitSyncService.validateToken(params);
    }
}
