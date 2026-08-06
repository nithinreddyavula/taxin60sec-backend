package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.SupportTicket;
import com.taxin60sec.backend.entity.enums.SupportTicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    Page<SupportTicket> findByRaisedBy_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<SupportTicket> findByRaisedBy_IdAndRelatedCase_IdOrderByCreatedAtDesc(Long userId, Long caseId, Pageable pageable);
    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(SupportTicketStatus status, Pageable pageable);
    Page<SupportTicket> findAllByOrderByCreatedAtDesc(Pageable pageable);
}