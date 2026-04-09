package com.ipl.analysis.controller;

import com.ipl.analysis.dto.PlayerDto;
import com.ipl.analysis.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Players", description = "Endpoints for player management")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    @Operation(summary = "Get all players paginated (Redis cached)", responses = {
            @ApiResponse(responseCode = "200", description = "List of players")
    })
    public ResponseEntity<Page<PlayerDto>> getAllPlayers(Pageable pageable) {
        return ResponseEntity.ok(playerService.getAllPlayers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Player details")
    })
    public ResponseEntity<PlayerDto> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search players by name", responses = {
            @ApiResponse(responseCode = "200", description = "Matching players")
    })
    public ResponseEntity<List<PlayerDto>> searchPlayers(@RequestParam String name) {
        return ResponseEntity.ok(playerService.searchPlayers(name));
    }

    @GetMapping("/team/{teamName}")
    @Operation(summary = "Get players by team", responses = {
            @ApiResponse(responseCode = "200", description = "Players in team")
    })
    public ResponseEntity<List<PlayerDto>> getPlayersByTeam(@PathVariable String teamName) {
        return ResponseEntity.ok(playerService.getPlayersByTeam(teamName));
    }

    @GetMapping("/top")
    @Operation(summary = "Get top players by role", responses = {
            @ApiResponse(responseCode = "200", description = "Top players")
    })
    public ResponseEntity<List<PlayerDto>> getTopPlayers(
            @RequestParam String role,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(playerService.getTopPlayers(role, limit));
    }
}
