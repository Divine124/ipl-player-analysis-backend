package com.ipl.analysis.scheduler;

import com.ipl.analysis.service.CricApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataSyncScheduler {

    private final CricApiService cricApiService;

    @Scheduled(fixedDelay = 1800000, initialDelay = 10000)
    @CacheEvict(value = {"players", "playerSearch", "teamPlayers", "topPlayers", "liveMatches", "todayMatches"}, allEntries = true)
    public void syncData() {
        log.info("Starting scheduled data sync...");
        try {
            cricApiService.syncLiveMatches();
            // In a real scenario, you'd iterate through known players or team squads
            // For demo purposes, we'll sync a few top players if they exist
            log.info("Data sync completed successfully. Caches evicted.");
        } catch (Exception e) {
            log.error("Error during scheduled data sync: {}", e.getMessage());
        }
    }
}
