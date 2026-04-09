package com.ipl.analysis.repository;

import com.ipl.analysis.model.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {
    Optional<AnalysisReport> findTopByPlayerIdOrderByCreatedAtDesc(Long playerId);
}
