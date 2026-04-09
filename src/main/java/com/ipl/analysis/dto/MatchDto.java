package com.ipl.analysis.dto;

import java.time.LocalDateTime;

public record MatchDto(
    Long id,
    String externalId,
    LocalDateTime matchDate,
    String team1,
    String team2,
    String venue,
    String status,
    String result
) {}
