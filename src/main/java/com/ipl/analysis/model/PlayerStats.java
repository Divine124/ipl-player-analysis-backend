package com.ipl.analysis.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(name = "matches_played")
    private Integer matchesPlayed;

    private Integer runs;
    private Integer wickets;

    @Column(name = "batting_average")
    private BigDecimal battingAverage;

    @Column(name = "strike_rate")
    private BigDecimal strikeRate;

    @Column(name = "bowling_average")
    private BigDecimal bowlingAverage;

    private BigDecimal economy;

    @Column(name = "highest_score")
    private Integer highestScore;

    @Column(name = "best_bowling_figures")
    private String bestBowlingFigures;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
