package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.SupportTicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketMessageRepository extends JpaRepository<SupportTicketMessage, Long> {
    List<SupportTicketMessage> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);
}