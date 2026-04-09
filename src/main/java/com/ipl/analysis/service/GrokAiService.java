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

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GrokAiService {

    private final WebClient.Builder webClientBuilder;
    private final SimpMessagingTemplate messagingTemplate;
    private final AnalysisReportRepository reportRepository;
    private final PlayerRepository playerRepository;
    private final ObjectMapper objectMapper;

    @Value("${grok.api.key}")
    private String apiKey;

    @Value("${grok.api.url}")
    private String apiUrl;

    @Value("${grok.api.model}")
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

        streamGrokResponse(prompt, sessionId, player);
    }

    private void streamGrokResponse(String prompt, String sessionId, Player player) {
        StringBuilder fullReport = new StringBuilder();

        WebClient webClient = webClientBuilder.build();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful cricket analyst."),
                        Map.of("role", "user", "content", prompt)
                ),
                "stream", true
        );

        webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> {
                    try {
                        if (chunk.equals("[DONE]")) return;
                        if (chunk.startsWith("data: ")) {
                            String jsonData = chunk.substring(6).trim();
                            if (jsonData.equals("[DONE]")) return;
                            
                            JsonNode node = objectMapper.readTree(jsonData);
                            if (node.has("choices") && node.get("choices").get(0).has("delta")) {
                                JsonNode delta = node.get("choices").get(0).get("delta");
                                if (delta.has("content")) {
                                    String text = delta.get("content").asText();
                                    fullReport.append(text);
                                    messagingTemplate.convertAndSend("/topic/analysis/" + sessionId, text);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Chunk processing skipped or failed: {}", e.getMessage());
                    }
                })
                .doOnComplete(() -> {
                    AnalysisReport report = AnalysisReport.builder()
                            .player(player)
                            .content(fullReport.toString())
                            .build();
                    reportRepository.save(report);
                    log.info("Analysis report (Grok) saved for player: {}", player.getName());
                })
                .subscribe();
    }
}
