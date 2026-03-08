package com.mzfuture.entire.checkpoint.mapper;

import com.mzfuture.entire.checkpoint.dto.response.CheckpointDTO;
import com.mzfuture.entire.checkpoint.dto.request.CheckpointUpdateParams;
import com.mzfuture.entire.checkpoint.entity.Checkpoint;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CheckpointMapper {

    CheckpointDTO toDTO(Checkpoint checkpoint);

    List<CheckpointDTO> toRows(List<Checkpoint> checkpoints);

    void updateEntity(CheckpointUpdateParams params, @MappingTarget Checkpoint checkpoint);
}
