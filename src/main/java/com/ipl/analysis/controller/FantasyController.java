package com.ipl.analysis.controller;

import com.ipl.analysis.dto.FantasyRequestDto;
import com.ipl.analysis.dto.FantasyResponseDto;
import com.ipl.analysis.service.FantasyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fantasy")
@RequiredArgsConstructor
@Tag(name = "Fantasy", description = "Endpoints for fantasy team recommendations")
public class FantasyController {

    private final FantasyService fantasyService;

    @PostMapping("/recommend")
    @Operation(summary = "Get best XI recommendation", responses = {
            @ApiResponse(responseCode = "200", description = "Best XI details")
    })
    public ResponseEntity<FantasyResponseDto> recommendXI(@RequestBody FantasyRequestDto request) {
        return ResponseEntity.ok(fantasyService.recommendXI(request));
    }

    @PostMapping("/captain")
    @Operation(summary = "Get captain/vice-captain recommendation", responses = {
            @ApiResponse(responseCode = "200", description = "Captain details")
    })
    public ResponseEntity<String> recommendCaptain(@RequestBody FantasyRequestDto request) {
        // Logic could be here or in service
        return ResponseEntity.ok("Captain selection analysis triggered.");
    }
}
