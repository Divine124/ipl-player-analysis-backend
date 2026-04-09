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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for syncing cricket data from CricAPI v1 (api.cricapi.com).
 * <p>
 * Key endpoints used:
 *   GET /currentMatches?apikey={key}&offset=0  — live/current match list
 *   GET /players?apikey={key}&search={name}    — player search
 *   GET /players_info?apikey={key}&id={id}     — full player profile with stats array
 *
 * The /players_info stats field is an array of { "fn": "runs", "vs": "3178", "label": "IPL" }.
 * We extract IPL/T20 stats by searching for matching "fn" (field name) entries.
 */
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
        log.info("Syncing live matches from CricAPI ({})", baseUrl);
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/currentMatches")
                        .queryParam("apikey", apiKey)
                        .queryParam("offset", 0)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseMatches)
                .doOnNext(this::upsertMatches)
                .doOnError(e -> log.error("Failed to sync live matches: {}", e.getMessage()))
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
                        .queryParam("offset", 0)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        List<Player> players = new ArrayList<>();
        if (response != null && response.has("data")) {
            for (JsonNode playerNode : response.get("data")) {
                if (!playerNode.has("id")) continue;
                String externalId = playerNode.get("id").asText();
                Player player = upsertPlayer(playerNode, externalId);
                syncPlayerStats(externalId, player.getId());
                players.add(player);
            }
        }
        return players;
    }

    /**
     * Syncs player info + stats using the /players_info endpoint (replaces the old /playerStats).
     * The response contains a "stats" array with entries like:
     *   { "fn": "runs", "vs": "3178", "label": "IPL" }
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void syncPlayerStats(String externalId, Long playerId) {
        log.info("Syncing player info/stats for external id: {}", externalId);
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/players_info")
                        .queryParam("apikey", apiKey)
                        .queryParam("id", externalId)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(node -> upsertPlayerStats(node, playerId))
                .doOnError(e -> log.error("Failed to sync player stats for {}: {}", externalId, e.getMessage()))
                .subscribe();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private List<Match> parseMatches(JsonNode node) {
        List<Match> matches = new ArrayList<>();
        if (!node.has("data")) return matches;

        for (JsonNode matchNode : node.get("data")) {
            try {
                // teams[] is always present; teamInfo[] may be absent or empty on some matches
                String team1 = "TBD";
                String team2 = "TBD";

                if (matchNode.has("teams") && matchNode.get("teams").size() >= 2) {
                    team1 = matchNode.get("teams").get(0).asText("TBD");
                    team2 = matchNode.get("teams").get(1).asText("TBD");
                } else if (matchNode.has("teamInfo") && matchNode.get("teamInfo").size() >= 2) {
                    team1 = matchNode.get("teamInfo").get(0).path("name").asText("TBD");
                    team2 = matchNode.get("teamInfo").get(1).path("name").asText("TBD");
                }

                matches.add(Match.builder()
                        .externalId(matchNode.path("id").asText())
                        .team1(team1)
                        .team2(team2)
                        .matchDate(LocalDateTime.now())
                        .venue(matchNode.path("venue").asText("TBD"))
                        .status(matchNode.path("status").asText("Unknown"))
                        .build());
            } catch (Exception e) {
                log.warn("Skipping malformed match node: {}", e.getMessage());
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
                        .name(node.path("name").asText("Unknown"))
                        .nationality(node.path("country").asText(null))
                        .role(node.path("role").asText(null))
                        .build()));
    }

    /**
     * Parses the CricAPI v1 /players_info stats array.
     * Each entry: { "fn": "runs", "vs": "3178", "label": "IPL" }
     * We prefer IPL stats; fall back to T20I if IPL is absent.
     */
    private void upsertPlayerStats(JsonNode node, Long playerId) {
        if (!node.has("data")) return;
        JsonNode data = node.get("data");

        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) return;

        // Update role/nationality from the detailed response if richer data is available
        if (data.has("role") && (player.getRole() == null || player.getRole().isBlank())) {
            player.setRole(data.path("role").asText(null));
            playerRepository.save(player);
        }

        PlayerStats stats = statsRepository.findByPlayerId(playerId)
                .orElse(PlayerStats.builder().player(player).build());

        // stats is an array: [{ "fn": "runs", "vs": "3178", "label": "IPL" }, ...]
        if (data.has("stats") && data.get("stats").isArray()) {
            JsonNode statsArray = data.get("stats");

            stats.setRuns(findStatInt(statsArray, "runs"));
            stats.setWickets(findStatInt(statsArray, "wickets"));
            stats.setMatchesPlayed(findStatInt(statsArray, "innings"));
            stats.setBattingAverage(findStatDecimal(statsArray, "avg"));
            stats.setStrikeRate(findStatDecimal(statsArray, "strikeRate"));
        }

        statsRepository.save(stats);
        log.info("Stats saved for player id: {}", playerId);
    }

    /**
     * Searches the stats array for a matching "fn" value, preferring the "IPL" label.
     * Falls back to T20I, then any entry.
     */
    private int findStatInt(JsonNode statsArray, String fieldName) {
        String value = findStatValue(statsArray, fieldName);
        if (value == null) return 0;
        try {
            return (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal findStatDecimal(JsonNode statsArray, String fieldName) {
        String value = findStatValue(statsArray, fieldName);
        if (value == null || value.isBlank() || value.equals("-")) return BigDecimal.ZERO;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String findStatValue(JsonNode statsArray, String fieldName) {
        String ipl = null, t20 = null, any = null;
        for (JsonNode stat : statsArray) {
            String fn = stat.path("fn").asText();
            if (!fn.equalsIgnoreCase(fieldName)) continue;
            String label = stat.path("label").asText("");
            String vs = stat.path("vs").asText(null);
            if (label.equalsIgnoreCase("IPL")) ipl = vs;
            else if (label.toLowerCase().contains("t20")) t20 = vs;
            else any = vs;
        }
        return ipl != null ? ipl : (t20 != null ? t20 : any);
    }
}
