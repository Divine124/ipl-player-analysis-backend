package com.ipl.analysis.dto;

public record AnalysisRequestDto(
    Long player1Id,
    Long player2Id,
    String sessionId
) {}
