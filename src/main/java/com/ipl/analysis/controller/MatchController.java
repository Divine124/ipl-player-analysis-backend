package com.ipl.analysis.controller;

import com.ipl.analysis.dto.MatchDto;
import com.ipl.analysis.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "Endpoints for match information")
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/live")
    @Operation(summary = "Get live IPL matches", responses = {
            @ApiResponse(responseCode = "200", description = "Live matches")
    })
    public ResponseEntity<List<MatchDto>> getLiveMatches() {
        return ResponseEntity.ok(matchService.getLiveMatches());
    }

    @GetMapping("/today")
    @Operation(summary = "Get matches for today", responses = {
            @ApiResponse(responseCode = "200", description = "Today's matches")
    })
    public ResponseEntity<List<MatchDto>> getTodayMatches() {
        return ResponseEntity.ok(matchService.getTodayMatches());
    }
}
