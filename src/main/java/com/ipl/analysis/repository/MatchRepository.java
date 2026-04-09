package com.ipl.analysis.repository;

import com.ipl.analysis.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByExternalId(String externalId);
    
    List<Match> findByMatchDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT m FROM Match m WHERE m.matchDate >= :start AND m.matchDate < :end")
    List<Match> findMatchesByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
