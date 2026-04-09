package com.ipl.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ipl.analysis.model.Match;
import com.ipl.analysis.model.Player;
import com.ipl.analysis.model.PlayerStats;
import com.ipl.analysis.repository.MatchRepository;
import com.ipl.analysis.repository.PlayerRepository;
import com.ipl.analysis.repository.PlayerStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CricApiService {

    private final WebClient.Builder webClientBuilder;
    private final PlayerRepository playerRepository;
    private final PlayerStatsRepository statsRepository;
    private final MatchRepository matchRepository;

    @Value("${cricapi.api.key}")
    private String apiKey;

    @Value("${cricapi.api.base-url}")
    private String baseUrl;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void syncLiveMatches() {
        log.info("Syncing live matches from CricAPI");
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/currentMatches")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseMatches)
                .doOnNext(this::upsertMatches)
                .subscribe();
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public List<Player> searchAndSyncPlayer(String name) {
        log.info("Searching and syncing player: {}", name);
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/players")
                        .queryParam("apikey", apiKey)
                        .queryParam("search", name)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        List<Player> players = new ArrayList<>();
        if (response != null && response.has("data")) {
            for (JsonNode playerNode : response.get("data")) {
                String externalId = playerNode.get("id").asText();
                Player player = upsertPlayer(playerNode, externalId);
                syncPlayerStats(externalId, player.getId());
                players.add(player);
            }
        }
        return players;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void syncPlayerStats(String externalId, Long playerId) {
        log.info("Syncing stats for player id: {}", externalId);
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/playerStats")
                        .queryParam("apikey", apiKey)
                        .queryParam("id", externalId)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(node -> upsertPlayerStats(node, playerId))
                .subscribe();
    }

    private List<Match> parseMatches(JsonNode node) {
        List<Match> matches = new ArrayList<>();
        if (node.has("data")) {
            for (JsonNode matchNode : node.get("data")) {
                matches.add(Match.builder()
                        .externalId(matchNode.get("id").asText())
                        .team1(matchNode.get("teamInfo").get(0).get("name").asText())
                        .team2(matchNode.get("teamInfo").get(1).get("name").asText())
                        .matchDate(LocalDateTime.now()) // Placeholder, parse actual date if available
                        .venue(matchNode.has("venue") ? matchNode.get("venue").asText() : "TBD")
                        .status(matchNode.get("status").asText())
                        .build());
            }
        }
        return matches;
    }

    private void upsertMatches(List<Match> matches) {
        for (Match match : matches) {
            matchRepository.findByExternalId(match.getExternalId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setStatus(match.getStatus());
                                matchRepository.save(existing);
                            },
                            () -> matchRepository.save(match)
                    );
        }
    }

    private Player upsertPlayer(JsonNode node, String externalId) {
        return playerRepository.findByExternalId(externalId)
                .orElseGet(() -> playerRepository.save(Player.builder()
                        .externalId(externalId)
                        .name(node.get("name").asText())
                        .nationality(node.has("country") ? node.get("country").asText() : null)
                        .build()));
    }

    private void upsertPlayerStats(JsonNode node, Long playerId) {
        if (!node.has("data")) return;
        JsonNode data = node.get("data");
        
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) return;

        PlayerStats stats = statsRepository.findByPlayerId(playerId)
                .orElse(PlayerStats.builder().player(player).build());

        // Simple mapping example, adjust based on actual CricAPI response structure
        // CricAPI response varies by player type, but let's assume some common fields
        stats.setMatchesPlayed(getInt(data, "matches"));
        stats.setRuns(getInt(data, "runs"));
        stats.setWickets(getInt(data, "wickets"));
        stats.setBattingAverage(getDecimal(data, "avg"));
        stats.setStrikeRate(getDecimal(data, "strikeRate"));

        statsRepository.save(stats);
    }

    private Integer getInt(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asInt() : 0;
    }

    private BigDecimal getDecimal(JsonNode node, String field) {
        return node.has(field) ? new BigDecimal(node.get(field).asText()) : BigDecimal.ZERO;
    }
}
