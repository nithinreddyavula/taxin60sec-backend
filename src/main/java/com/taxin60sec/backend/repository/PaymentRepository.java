package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.dto.domain.CaPayoutSummaryDto;
import com.taxin60sec.backend.entity.Payment;
import com.taxin60sec.backend.entity.enums.EscrowStatus;
import com.taxin60sec.backend.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByClientIdAndDeletedFalseOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    Page<Payment> findByRelatedCase_IdAndDeletedFalseOrderByCreatedAtDesc(Long caseId, Pageable pageable);

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    List<Payment> findByRelatedCase_IdAndEscrowStatus(Long caseId, EscrowStatus escrowStatus);

    List<Payment> findByRelatedCase_IdAndEscrowStatusIn(Long caseId, List<EscrowStatus> escrowStatuses);

    List<Payment> findByRelatedCase_AssignedCa_IdAndEscrowStatusIsNotNullAndDeletedFalseOrderByCreatedAtDesc(Long assignedCaId);

    @Query("select new com.taxin60sec.backend.dto.domain.CaPayoutSummaryDto(" +
            "p.relatedCase.assignedCa.id, " +
            "p.relatedCase.assignedCa.fullName, " +
            "coalesce(sum(p.caPayoutAmount), 0), " +
            "coalesce(sum(case when p.escrowStatus in (com.taxin60sec.backend.entity.enums.EscrowStatus.HELD, com.taxin60sec.backend.entity.enums.EscrowStatus.PARTIALLY_RELEASED) then p.amount else 0 end), 0), " +
            "coalesce(sum(p.platformCommissionAmount), 0), " +
            "count(p)) " +
            "from Payment p " +
            "where p.relatedCase.assignedCa.id is not null and p.escrowStatus is not null and p.deleted = false " +
            "group by p.relatedCase.assignedCa.id, p.relatedCase.assignedCa.fullName")
    List<CaPayoutSummaryDto> payoutSummaryByCa();

    @Query("select " +
            "coalesce(sum(case when p.status = com.taxin60sec.backend.entity.enums.PaymentStatus.SUCCESS then p.amount else 0 end), 0), " +
            "coalesce(sum(p.caPayoutAmount), 0), " +
            "coalesce(sum(p.platformCommissionAmount), 0), " +
            "coalesce(sum(case when p.escrowStatus in (com.taxin60sec.backend.entity.enums.EscrowStatus.HELD, com.taxin60sec.backend.entity.enums.EscrowStatus.PARTIALLY_RELEASED) then p.amount else 0 end), 0), " +
            "coalesce(sum(case when p.status = com.taxin60sec.backend.entity.enums.PaymentStatus.REFUNDED then p.amount else 0 end), 0) " +
            "from Payment p where p.deleted = false")
    List<Object[]> revenueTotals();
}