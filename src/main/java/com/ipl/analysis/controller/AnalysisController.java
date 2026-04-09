package com.ipl.analysis.controller;

import com.ipl.analysis.dto.AnalysisRequestDto;
import com.ipl.analysis.dto.AnalysisResponseDto;
import com.ipl.analysis.model.AnalysisReport;
import com.ipl.analysis.repository.AnalysisReportRepository;
import com.ipl.analysis.service.GrokAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "Endpoints for AI-powered player analysis")
public class AnalysisController {

    private final GrokAiService grokAiService;
    private final AnalysisReportRepository reportRepository;

    @PostMapping("/player/{id}")
    @Operation(summary = "Trigger Claude AI analysis for a player", responses = {
            @ApiResponse(responseCode = "202", description = "Analysis triggered")
    })
    public ResponseEntity<String> analysePlayer(@PathVariable Long id, @RequestParam String sessionId) {
        grokAiService.analysePlayerPerformance(id, sessionId);
        return ResponseEntity.accepted().body("Analysis triggered. Results will be streamed via WebSocket.");
    }

    @GetMapping("/player/{id}/latest")
    @Operation(summary = "Get latest analysis report for a player", responses = {
            @ApiResponse(responseCode = "200", description = "Latest report"),
            @ApiResponse(responseCode = "404", description = "No report found")
    })
    public ResponseEntity<AnalysisResponseDto> getLatestReport(@PathVariable Long id) {
        return reportRepository.findTopByPlayerIdOrderByCreatedAtDesc(id)
                .map(report -> new AnalysisResponseDto(report.getId(), report.getPlayer().getId(), report.getContent(), report.getCreatedAt()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/compare")
    @Operation(summary = "Compare two players using AI", responses = {
            @ApiResponse(responseCode = "200", description = "Comparison result")
    })
    public ResponseEntity<String> comparePlayers(@RequestBody AnalysisRequestDto request) {
        // Logic for comparison could also be an async streaming call
        return ResponseEntity.ok("Comparison triggered via WebSocket.");
    }
}
