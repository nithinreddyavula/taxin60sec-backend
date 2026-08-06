package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.compliance.ComplianceScoreResponse;
import com.taxin60sec.backend.entity.ComplianceObligation;
import com.taxin60sec.backend.entity.enums.ComplianceStatus;
import com.taxin60sec.backend.entity.enums.ComplianceType;
import com.taxin60sec.backend.repository.ComplianceObligationRepository;
import com.taxin60sec.backend.repository.ServiceOfferingRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComplianceScoreServiceTest {

    private ComplianceObligation obligation(
            ComplianceType type,
            LocalDate dueDate,
            ComplianceStatus status,
            Instant completedAt
    ) {
        ComplianceObligation o = new ComplianceObligation();
        o.setType(type);
        o.setTitle(type.name());
        o.setDueDate(dueDate);
        o.setStatus(status);
        o.setCompletedAt(completedAt);
        return o;
    }

    @Test
    void returnsPerfectScoreWhenNoObligationsTracked() {
        ComplianceObligationRepository repo = mock(ComplianceObligationRepository.class);
        when(repo.findByClientIdAndDeletedFalseOrderByDueDateAsc(1L)).thenReturn(List.of());

        ComplianceScoreService service = new ComplianceScoreService(repo, mock(ServiceOfferingRepository.class));
        ComplianceScoreResponse response = service.scoreFor(1L);

        assertEquals(100, response.score());
        assertEquals("Healthy", response.statusLabel());
    }

    @Test
    void deductsForOverdueObligation() {
        ComplianceObligationRepository repo = mock(ComplianceObligationRepository.class);
        when(repo.findByClientIdAndDeletedFalseOrderByDueDateAsc(1L)).thenReturn(List.of(
                obligation(ComplianceType.GST_RETURN, LocalDate.now().minusDays(2), ComplianceStatus.OVERDUE, null)
        ));

        ComplianceScoreService service = new ComplianceScoreService(repo, mock(ServiceOfferingRepository.class));
        ComplianceScoreResponse response = service.scoreFor(1L);

        assertEquals(85, response.score());
    }

    @Test
    void deductsForLateCompletion() {
        ComplianceObligationRepository repo = mock(ComplianceObligationRepository.class);
        LocalDate due = LocalDate.now().minusDays(5);
        Instant completedLate = due.plusDays(2).atStartOfDay(ZoneOffset.UTC).toInstant();

        when(repo.findByClientIdAndDeletedFalseOrderByDueDateAsc(1L)).thenReturn(List.of(
                obligation(ComplianceType.ITR_FILING, due, ComplianceStatus.COMPLETED, completedLate)
        ));

        ComplianceScoreService service = new ComplianceScoreService(repo, mock(ServiceOfferingRepository.class));
        ComplianceScoreResponse response = service.scoreFor(1L);

        assertEquals(95, response.score());
    }

    @Test
    void selfHealsPendingPastDueIntoOverdue() {
        ComplianceObligationRepository repo = mock(ComplianceObligationRepository.class);
        ComplianceObligation pastDuePending = obligation(
                ComplianceType.ROC_FILING, LocalDate.now().minusDays(1), ComplianceStatus.PENDING, null
        );
        when(repo.findByClientIdAndDeletedFalseOrderByDueDateAsc(1L)).thenReturn(List.of(pastDuePending));

        ComplianceScoreService service = new ComplianceScoreService(repo, mock(ServiceOfferingRepository.class));
        service.scoreFor(1L);

        assertEquals(ComplianceStatus.OVERDUE, pastDuePending.getStatus());
    }

    @Test
    void scoreNeverGoesBelowZero() {
        ComplianceObligationRepository repo = mock(ComplianceObligationRepository.class);
        List<ComplianceObligation> manyOverdue = List.of(
                obligation(ComplianceType.GST_RETURN, LocalDate.now().minusDays(10), ComplianceStatus.OVERDUE, null),
                obligation(ComplianceType.TDS_RETURN, LocalDate.now().minusDays(10), ComplianceStatus.OVERDUE, null),
                obligation(ComplianceType.ROC_FILING, LocalDate.now().minusDays(10), ComplianceStatus.OVERDUE, null),
                obligation(ComplianceType.ITR_FILING, LocalDate.now().minusDays(10), ComplianceStatus.OVERDUE, null),
                obligation(ComplianceType.ADVANCE_TAX, LocalDate.now().minusDays(10), ComplianceStatus.OVERDUE, null),
                obligation(ComplianceType.OTHER, LocalDate.now().minusDays(10), ComplianceStatus.OVERDUE, null),
                obligation(ComplianceType.OTHER, LocalDate.now().minusDays(10), ComplianceStatus.OVERDUE, null)
        );
        when(repo.findByClientIdAndDeletedFalseOrderByDueDateAsc(1L)).thenReturn(manyOverdue);

        ComplianceScoreService service = new ComplianceScoreService(repo, mock(ServiceOfferingRepository.class));
        ComplianceScoreResponse response = service.scoreFor(1L);

        assertEquals(0, response.score());
        assertEquals("At risk", response.statusLabel());
    }
}