package com.mzfuture.entire.gitrepo.controller;


import com.mzfuture.entire.gitrepo.dto.request.RepoCreateParams;
import com.mzfuture.entire.gitrepo.dto.response.RepoDTO;
import com.mzfuture.entire.gitrepo.dto.request.RepoSearchParams;
import com.mzfuture.entire.gitrepo.dto.request.RepoUpdateParams;
import com.mzfuture.entire.gitrepo.service.RepoService;
import com.mzfuture.entire.common.dto.IdPayload;
import com.mzfuture.entire.common.dto.PagerPayload;
import com.mzfuture.entire.common.dto.StatusResult;
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
import org.springframework.web.bind.annotation.*;

/**
 * Repository Controller
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/repo")
@Tag(name = "Repository Management", description = "Repository CRUD APIs")
public class GitRepoController {

    private final RepoService RepoService;

    /**
     * Create repository
     */
    @PostMapping("/create")
    @Operation(summary = "Create repository", description = "Create a new repository")
    public RepoDTO create(@RequestBody @Valid RepoCreateParams params) {
        return RepoService.create(params);
    }

    /**
     * Update repository
     */
    @PostMapping("/update")
    @Operation(summary = "Update repository", description = "Update repository information")
    public RepoDTO update(@RequestBody @Valid RepoUpdateParams params) {
        return RepoService.update(params);
    }

    /**
     * Get single repository
     */
    @GetMapping("/get")
    @Operation(summary = "Get single repository", description = "Get repository details by ID")
    public RepoDTO get(@ModelAttribute @Valid IdPayload payload) {
        return RepoService.get(payload.getId());
    }

    /**
     * Delete repository
     */
    @PostMapping("/delete")
    @Operation(summary = "Delete repository", description = "Delete repository by ID")
    public StatusResult<Void> delete(@RequestBody @Valid IdPayload payload) {
        RepoService.delete(payload.getId());
        return StatusResult.ok();
    }

    /**
     * Search repositories
     */
    @GetMapping("/search")
    @Operation(summary = "Search repositories", description = "Search repositories by keyword (paginated)")
    public PagerPayload<RepoDTO> search(
            @ModelAttribute @Valid RepoSearchParams params,
            @Parameter(description = "Pagination parameters: page (starting from 0), size (items per page), sort (sort field)", example = "page=0&size=10&sort=createdAt,desc")
            @PageableDefault(sort = {"createdAt"}, size = 20, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RepoDTO> page = RepoService.search(params, pageable);
        return new PagerPayload<>(page, pageable);
    }

}

