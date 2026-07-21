package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CaseRepository extends JpaRepository<Case, Long>, JpaSpecificationExecutor<Case> {
    Optional<Case> findByCaseNumber(String caseNumber);
    Optional<Case> findByPublicAccessToken(String token);
    long count();

long countByStatus(CaseStatus status);

List<Case> findAllByOrderByCreatedAtDesc();
    Optional<Case> findFirstByClientIdAndArchivedFalseAndDeletedFalseOrderByUpdatedAtDesc(Long clientId);
    Optional<Case> findFirstByClientIdAndServiceOfferingIdAndArchivedFalseAndDeletedFalseOrderByUpdatedAtDesc(Long clientId, Long serviceOfferingId);
}
