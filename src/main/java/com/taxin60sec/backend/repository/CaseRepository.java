package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import java.util.List;

public interface CaseRepository extends JpaRepository<Case, Long>, JpaSpecificationExecutor<Case> {
    Optional<Case> findByCaseNumber(String caseNumber);
    Optional<Case> findByPublicAccessToken(String token);
    long count();

    long countByStatus(CaseStatus status);

    List<Case> findAllByOrderByCreatedAtDesc();
    List<Case> findTop200ByFirstResponseAtIsNotNullAndDeletedFalseOrderByFirstResponseAtDesc();
    Optional<Case> findFirstByClientIdAndArchivedFalseAndDeletedFalseOrderByUpdatedAtDesc(Long clientId);
    Optional<Case> findFirstByClientIdAndServiceOfferingIdAndArchivedFalseAndDeletedFalseOrderByUpdatedAtDesc(Long clientId, Long serviceOfferingId);
    /** Used by the admin client detail screen and the clients Excel export. */
    List<Case> findByClient_IdAndDeletedFalseOrderByCreatedAtDesc(Long clientId);

    @org.springframework.data.jpa.repository.Query("select count(distinct c.client.id) from Case c where c.deleted = false")
    long countDistinctClients();

    /** Active caseload for a CA - excludes COMPLETED/CANCELLED so a CA's finished work doesn't count against them. */
    long countByAssignedCa_IdAndDeletedFalseAndStatusNotIn(Long assignedCaId, java.util.Collection<CaseStatus> excludedStatuses);

    /** Case volume grouped by service category, for the admin Reports screen. */
    @org.springframework.data.jpa.repository.Query("select new com.taxin60sec.backend.dto.admin.CaseVolumeByCategoryDto(" +
            "coalesce(c.serviceOffering.category, com.taxin60sec.backend.entity.enums.ServiceCategory.OTHER), count(c)) " +
            "from Case c where c.deleted = false group by c.serviceOffering.category")
    List<com.taxin60sec.backend.dto.admin.CaseVolumeByCategoryDto> volumeByCategory();

    /** Average days from assignment to completion across completed cases. */
    @org.springframework.data.jpa.repository.Query(value =
            "select avg(extract(epoch from (completed_at - assigned_at)) / 86400.0) " +
                    "from cases where deleted = false and completed_at is not null and assigned_at is not null",
            nativeQuery = true)
    Double averageTurnaroundDays();

    long countByClientIdAndDeletedFalse(Long clientId);
}