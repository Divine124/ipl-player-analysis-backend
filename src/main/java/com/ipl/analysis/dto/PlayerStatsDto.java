package com.ipl.analysis.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlayerStatsDto(
    Integer matchesPlayed,
    Integer runs,
    Integer wickets,
    BigDecimal battingAverage,
    BigDecimal strikeRate,
    BigDecimal bowlingAverage,
    BigDecimal economy,
    Integer highestScore,
    String bestBowlingFigures,
    LocalDateTime updatedAt
) {}
