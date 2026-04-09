package com.ipl.analysis.dto;

import java.time.LocalDateTime;

public record AnalysisResponseDto(
    Long id,
    Long playerId,
    String content,
    LocalDateTime createdAt
) {}
