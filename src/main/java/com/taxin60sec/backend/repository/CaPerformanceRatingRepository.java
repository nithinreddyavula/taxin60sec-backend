package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.CaPerformanceRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaPerformanceRatingRepository extends JpaRepository<CaPerformanceRating, Long> {
    Optional<CaPerformanceRating> findByCaseRefId(Long caseId);
    List<CaPerformanceRating> findByCaIdOrderByRatedAtDesc(Long caId);
}