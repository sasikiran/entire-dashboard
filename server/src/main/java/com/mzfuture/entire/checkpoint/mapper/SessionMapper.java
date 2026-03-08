package com.mzfuture.entire.checkpoint.mapper;

import com.mzfuture.entire.checkpoint.dto.response.SessionDTO;
import com.mzfuture.entire.checkpoint.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SessionMapper {

    SessionDTO toDTO(Session session);

    List<SessionDTO> toRows(List<Session> sessions);
}
