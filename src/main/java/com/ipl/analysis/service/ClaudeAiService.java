package com.ipl.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipl.analysis.model.AnalysisReport;
import com.ipl.analysis.model.Player;
import com.ipl.analysis.repository.AnalysisReportRepository;
import com.ipl.analysis.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClaudeAiService {

    private final WebClient.Builder webClientBuilder;
    private final SimpMessagingTemplate messagingTemplate;
    private final AnalysisReportRepository reportRepository;
    private final PlayerRepository playerRepository;
    private final ObjectMapper objectMapper;

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    @Value("${claude.api.model}")
    private String model;

    public void analysePlayerPerformance(Long playerId, String sessionId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        String prompt = String.format(
                "You are an expert IPL cricket analyst. Given the player stats below, provide:\n" +
                "1) Form rating /10 with reasoning\n" +
                "2) Key strengths and weaknesses for IPL conditions\n" +
                "3) Performance prediction for next 3 matches\n" +
                "4) Fantasy pick verdict: MUST PICK / RISKY / AVOID with reason\n" +
                "5) One-line brutal honest verdict\n\n" +
                "Stats for %s:\n" +
                "Matches: %d, Runs: %d, Wickets: %d, Batting Avg: %s, Strike Rate: %s",
                player.getName(),
                player.getStats().getMatchesPlayed(),
                player.getStats().getRuns(),
                player.getStats().getWickets(),
                player.getStats().getBattingAverage(),
                player.getStats().getStrikeRate()
        );

        streamClaudeResponse(prompt, sessionId, player);
    }

    private void streamClaudeResponse(String prompt, String sessionId, Player player) {
        StringBuilder fullReport = new StringBuilder();

        WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 1024,
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                },
                "stream", true
        );

        webClient.post()
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(chunk -> !chunk.equals("[DONE]"))
                .doOnNext(chunk -> {
                    try {
                        if (chunk.startsWith("data: ")) {
                            String jsonData = chunk.substring(6).trim();
                            JsonNode node = objectMapper.readTree(jsonData);
                            if (node.has("delta") && node.get("delta").has("text")) {
                                String text = node.get("delta").get("text").asText();
                                fullReport.append(text);
                                messagingTemplate.convertAndSend("/topic/analysis/" + sessionId, text);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error parsing Claude response chunk: {}", e.getMessage());
                    }
                })
                .doOnComplete(() -> {
                    AnalysisReport report = AnalysisReport.builder()
                            .player(player)
                            .content(fullReport.toString())
                            .build();
                    reportRepository.save(report);
                    log.info("Analysis report saved for player: {}", player.getName());
                })
                .subscribe();
    }
}
