package com.ipl.analysis.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false)
    private String externalId; // CricAPI ID

    @Column(nullable = false)
    private String name;

    private String team;
    private String role;
    private String nationality;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    private PlayerStats stats;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<AnalysisReport> reports;
}
