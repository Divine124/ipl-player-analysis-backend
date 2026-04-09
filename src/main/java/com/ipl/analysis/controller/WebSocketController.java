package com.ipl.analysis.controller;

import com.ipl.analysis.dto.AnalysisRequestDto;
import com.ipl.analysis.service.ClaudeAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ClaudeAiService claudeAiService;

    @MessageMapping("/analyse")
    public void analyse(AnalysisRequestDto request) {
        claudeAiService.analysePlayerPerformance(request.player1Id(), request.sessionId());
    }
}
