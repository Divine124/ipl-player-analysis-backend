package com.ipl.analysis.dto;

import java.util.List;

public record FantasyResponseDto(
    String recommendation,
    List<String> selectedXI,
    String captain,
    String viceCaptain,
    List<String> riskyPicks,
    String differentialPick
) {}
