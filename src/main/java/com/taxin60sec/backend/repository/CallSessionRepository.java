package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.CallSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CallSessionRepository extends JpaRepository<CallSession, Long> {
    List<CallSession> findByCaseRef_IdOrderByCreatedAtDesc(Long caseId);
}