package com.mzfuture.entire.gitrepo.mapper;

import com.mzfuture.entire.gitrepo.dto.request.RepoCreateParams;
import com.mzfuture.entire.gitrepo.dto.response.RepoDTO;
import com.mzfuture.entire.gitrepo.dto.request.RepoUpdateParams;
import com.mzfuture.entire.gitrepo.entity.Repo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RepoMapper {
    RepoDTO toDTO(Repo repo);

    List<RepoDTO> toRows(List<Repo> repos);

    Repo toEntity(RepoCreateParams params);

    void updateEntity(RepoUpdateParams params, @MappingTarget Repo repo);
}
