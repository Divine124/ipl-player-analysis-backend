package com.ipl.analysis.service;

import com.ipl.analysis.dto.MatchDto;
import com.ipl.analysis.mapper.MatchMapper;
import com.ipl.analysis.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final CricApiService cricApiService;

    @Cacheable(value = "liveMatches")
    public List<MatchDto> getLiveMatches() {
        // Optionally trigger a sync before returning, or rely on scheduler
        return matchRepository.findMatchesByDate(
                LocalDateTime.now().minusHours(6), 
                LocalDateTime.now().plusHours(6)
        ).stream().map(matchMapper::toDto).collect(Collectors.toList());
    }

    @Cacheable(value = "todayMatches")
    public List<MatchDto> getTodayMatches() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return matchRepository.findByMatchDateBetween(startOfDay, endOfDay)
                .stream()
                .map(matchMapper::toDto)
                .collect(Collectors.toList());
    }
}
