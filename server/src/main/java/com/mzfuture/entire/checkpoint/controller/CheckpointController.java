package com.mzfuture.entire.checkpoint.controller;

import com.mzfuture.entire.checkpoint.dto.request.CheckpointFilterParams;
import com.mzfuture.entire.checkpoint.dto.response.CheckpointDTO;
import com.mzfuture.entire.checkpoint.dto.request.CheckpointSearchParams;
import com.mzfuture.entire.checkpoint.dto.response.RepoOptionDTO;
import com.mzfuture.entire.checkpoint.service.CheckpointService;
import com.mzfuture.entire.checkpoint.service.CheckpointSyncService;
import com.mzfuture.entire.common.dto.IdPayload;
import com.mzfuture.entire.common.dto.PagerPayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/// Checkpoint API: get by id and search by branch/commit message (paginated)
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/checkpoint")
@Tag(name = "Checkpoint", description = "Checkpoint get and search APIs")
public class CheckpointController {

    private final CheckpointService checkpointService;
    private final CheckpointSyncService checkpointSyncService;

    /// Get single checkpoint by ID
    @GetMapping("/get")
    @Operation(summary = "Get checkpoint", description = "Get full checkpoint details by ID")
    public CheckpointDTO get(@ModelAttribute @Valid IdPayload payload) {
        return checkpointService.get(payload.getId());
    }

    /// Get distinct repos for filter dropdown (by time range and optional filters)
    @GetMapping("/repos")
    @Operation(summary = "Get repos for filter", description = "Returns distinct repos from checkpoints matching the filter (startTime, endTime required)")
    public List<RepoOptionDTO> getRepos(@ModelAttribute @Valid CheckpointFilterParams params) {
        return checkpointService.getReposForFilter(params);
    }

    /// Get distinct commit authors for filter dropdown
    @GetMapping("/commit-authors")
    @Operation(summary = "Get commit authors for filter", description = "Returns distinct commit author names from checkpoints matching the filter")
    public List<String> getCommitAuthors(@ModelAttribute @Valid CheckpointFilterParams params) {
        return checkpointService.getCommitAuthorsForFilter(params);
    }

    /// Search checkpoints by repoIds, commitAuthorNames, branch, commit message, time range (paginated)
    @GetMapping("/search")
    @Operation(summary = "Search checkpoints", description = "Search checkpoints by repoIds, commitAuthorNames, branch, commit message (fuzzy), startTime/endTime, paginated")
    public PagerPayload<CheckpointDTO> search(
            @ModelAttribute @Valid CheckpointSearchParams params,
            @Parameter(description = "Pagination: page (0-based), size, sort (e.g. commitTime,desc)")
            @PageableDefault(sort = {"commitTime"}, size = 20, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CheckpointDTO> page = checkpointService.search(params, pageable);
        return new PagerPayload<>(page, pageable);
    }

    /// Trigger checkpoint sync for one repo (incremental unless fullScan=true)
    @PostMapping("/sync/repo/{repoId}")
    @Operation(summary = "Sync repo checkpoints", description = "Immediately sync checkpoints for the given repo. Use fullScan=true to walk all commits.")
    public void syncRepo(
            @PathVariable Long repoId,
            @RequestParam(required = false, defaultValue = "false") boolean fullScan) {
        checkpointSyncService.syncRepo(repoId, fullScan);
    }

    /// Trigger checkpoint sync for all repos
    @PostMapping("/sync/all")
    @Operation(summary = "Sync all checkpoints", description = "Immediately sync checkpoints for all repos. Use fullScan=true to full-scan each repo.")
    public void syncAll(@RequestParam(required = false, defaultValue = "false") boolean fullScan) {
        checkpointSyncService.syncAllRepos(fullScan);
    }
}
