package com.mzfuture.entire.gitrepo.service;

import com.mzfuture.entire.gitrepo.dto.request.RepoCreateParams;
import com.mzfuture.entire.gitrepo.dto.response.RepoDTO;
import com.mzfuture.entire.gitrepo.dto.request.RepoSearchParams;
import com.mzfuture.entire.gitrepo.dto.request.RepoUpdateParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RepoService {
    RepoDTO create(RepoCreateParams params);

    RepoDTO update(RepoUpdateParams params);

    RepoDTO get(Long id);

    void delete(Long id);

    Page<RepoDTO> search(RepoSearchParams params, Pageable pageable);
}
