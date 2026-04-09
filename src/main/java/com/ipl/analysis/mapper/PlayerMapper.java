package com.ipl.analysis.mapper;

import com.ipl.analysis.dto.PlayerDto;
import com.ipl.analysis.dto.PlayerStatsDto;
import com.ipl.analysis.model.Player;
import com.ipl.analysis.model.PlayerStats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
    @Mapping(target = "stats", source = "stats")
    PlayerDto toDto(Player player);

    PlayerStatsDto toStatsDto(PlayerStats stats);
}
