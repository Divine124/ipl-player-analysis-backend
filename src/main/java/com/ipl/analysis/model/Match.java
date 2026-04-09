package com.ipl.analysis.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false)
    private String externalId;

    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    @Column(nullable = false)
    private String team1;

    @Column(nullable = false)
    private String team2;

    private String venue;
    private String status;
    private String result;
}
