package com.ipl.analysis.repository;

import com.ipl.analysis.model.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByExternalId(String externalId);
    
    Page<Player> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    List<Player> findByTeam(String team);
    
    @Query("SELECT p FROM Player p JOIN p.stats s WHERE p.role = :role ORDER BY s.runs DESC")
    List<Player> findTopBatsmen(@Param("role") String role, Pageable pageable);

    @Query("SELECT p FROM Player p JOIN p.stats s WHERE p.role = :role ORDER BY s.wickets DESC")
    List<Player> findTopBowlers(@Param("role") String role, Pageable pageable);
}
