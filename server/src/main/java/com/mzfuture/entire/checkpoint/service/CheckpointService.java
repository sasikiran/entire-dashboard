package com.mzfuture.entire.checkpoint.service;

import com.mzfuture.entire.checkpoint.dto.request.CheckpointFilterParams;
import com.mzfuture.entire.checkpoint.dto.response.CheckpointDTO;
import com.mzfuture.entire.checkpoint.dto.request.CheckpointSearchParams;
import com.mzfuture.entire.checkpoint.dto.request.CheckpointUpdateParams;
import com.mzfuture.entire.checkpoint.dto.response.RepoOptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CheckpointService {

    CheckpointDTO update(CheckpointUpdateParams params);

    CheckpointDTO get(Long id);

    void delete(Long id);

    Page<CheckpointDTO> search(CheckpointSearchParams params, Pageable pageable);

    List<RepoOptionDTO> getReposForFilter(CheckpointFilterParams params);

    List<String> getCommitAuthorsForFilter(CheckpointFilterParams params);
}
