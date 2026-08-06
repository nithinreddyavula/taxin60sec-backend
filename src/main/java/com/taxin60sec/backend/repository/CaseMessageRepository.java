package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.CaseMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseMessageRepository extends JpaRepository<CaseMessage, Long> {
    List<CaseMessage> findByCaseRef_IdOrderByCreatedAtAsc(Long caseId);
    long countByCaseRef_IdAndReadAtIsNull(Long caseId);
}