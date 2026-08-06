package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.ComplianceObligation;
import com.taxin60sec.backend.entity.enums.ComplianceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ComplianceObligationRepository extends JpaRepository<ComplianceObligation, Long> {

    List<ComplianceObligation> findByClientIdAndDeletedFalseOrderByDueDateAsc(Long clientId);

    List<ComplianceObligation> findByRelatedCaseIdAndDeletedFalse(Long caseId);

    List<ComplianceObligation> findByStatusAndDeletedFalseAndDueDateBetween(
            ComplianceStatus status,
            LocalDate from,
            LocalDate to
    );

    List<ComplianceObligation> findByStatusAndDeletedFalseAndDueDateBefore(
            ComplianceStatus status,
            LocalDate date
    );

    long countByDeletedFalse();

    long countByStatusAndDeletedFalse(ComplianceStatus status);
}