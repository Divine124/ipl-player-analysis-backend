package com.ipl.analysis.mapper;

import com.ipl.analysis.dto.MatchDto;
import com.ipl.analysis.model.Match;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MatchMapper {
    MatchDto toDto(Match match);
}
