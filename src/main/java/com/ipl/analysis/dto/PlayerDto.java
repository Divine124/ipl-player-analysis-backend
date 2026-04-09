package com.ipl.analysis.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlayerDto(
    Long id,
    String externalId,
    String name,
    String team,
    String role,
    String nationality,
    PlayerStatsDto stats
) {}
