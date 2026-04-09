package com.ipl.analysis.dto;

import java.util.List;

public record FantasyRequestDto(
    Long matchId,
    List<String> squadPlayerIds
) {}
