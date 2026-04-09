package com.ipl.analysis.repository;

import com.ipl.analysis.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByExternalId(String externalId);
    
    List<Match> findByMatchDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT m FROM Match m WHERE m.matchDate >= :start AND m.matchDate < :end")
    List<Match> findMatchesByDate(LocalDateTime start, LocalDateTime end);
}
