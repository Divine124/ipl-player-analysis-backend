package com.ipl.analysis.controller;

import com.ipl.analysis.dto.AnalysisRequestDto;
import com.ipl.analysis.service.GrokAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final GrokAiService grokAiService;

    @MessageMapping("/analyse")
    public void analyse(AnalysisRequestDto request) {
        grokAiService.analysePlayerPerformance(request.player1Id(), request.sessionId());
    }
}
