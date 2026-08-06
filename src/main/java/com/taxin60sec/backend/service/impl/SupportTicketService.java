package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.dto.business.SupportTicketRequests;
import com.taxin60sec.backend.dto.domain.SupportTicketDto;
import com.taxin60sec.backend.dto.domain.SupportTicketMessageDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.SupportTicket;
import com.taxin60sec.backend.entity.SupportTicketMessage;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.SupportTicketStatus;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.SupportTicketMessageRepository;
import com.taxin60sec.backend.repository.SupportTicketRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * The "Support" channel in the case workspace, distinct from the in-case CA chat
 * (CommunicationService) - this is for general platform support, not case-specific
 * questions to the assigned CA.
 */
@Service
@Transactional
public class SupportTicketService {

    private final SupportTicketRepository tickets;
    private final SupportTicketMessageRepository messages;
    private final CaseRepository cases;

    public SupportTicketService(SupportTicketRepository tickets, SupportTicketMessageRepository messages, CaseRepository cases) {
        this.tickets = tickets;
        this.messages = messages;
        this.cases = cases;
    }

    public SupportTicketDto create(SupportTicketRequests.Create request, User raisedBy) {
        SupportTicket ticket = new SupportTicket();
        ticket.setRaisedBy(raisedBy);
        ticket.setSubject(request.subject());

        if (request.caseId() != null) {
            Case c = cases.findById(request.caseId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Case not found"));
            ticket.setRelatedCase(c);
        }

        SupportTicket saved = tickets.save(ticket);

        SupportTicketMessage firstMessage = new SupportTicketMessage();
        firstMessage.setTicket(saved);
        firstMessage.setSender(raisedBy);
        firstMessage.setBody(request.message());
        messages.save(firstMessage);

        return toDto(saved);
    }

    public SupportTicketMessageDto reply(Long ticketId, SupportTicketRequests.Reply request, User sender) {
        SupportTicket ticket = ticketById(ticketId);

        SupportTicketMessage message = new SupportTicketMessage();
        message.setTicket(ticket);
        message.setSender(sender);
        message.setBody(request.body());

        return toDto(messages.save(message));
    }

    public List<SupportTicketMessageDto> listMessages(Long ticketId, User requester) {
        SupportTicket ticket = ticketById(ticketId);
        ensureAccess(ticket, requester);
        return messages.findByTicket_IdOrderByCreatedAtAsc(ticketId).stream().map(this::toDto).toList();
    }

    public PageResponse<SupportTicketDto> myTickets(Long userId, Long caseId, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size);
        var result = caseId == null
                ? tickets.findByRaisedBy_IdOrderByCreatedAtDesc(userId, pageable)
                : tickets.findByRaisedBy_IdAndRelatedCase_IdOrderByCreatedAtDesc(userId, caseId, pageable);
        return new PageResponse<>(result.getContent().stream().map(this::toDto).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public PageResponse<SupportTicketDto> allTickets(SupportTicketStatus status, int page, int size) {
        var result = status == null
                ? tickets.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size))
                : tickets.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size));
        return new PageResponse<>(result.getContent().stream().map(this::toDto).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public SupportTicketDto setStatus(Long ticketId, SupportTicketStatus status) {
        SupportTicket ticket = ticketById(ticketId);
        ticket.setStatus(status);
        return toDto(ticket);
    }

    private void ensureAccess(SupportTicket ticket, User requester) {
        boolean isOwner = Objects.equals(ticket.getRaisedBy().getId(), requester.getId());
        boolean isAdmin = requester.getRoles() != null
                && requester.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()));
        if (!isOwner && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "Ticket does not belong to current user");
        }
    }

    private SupportTicket ticketById(Long id) {
        return tickets.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Ticket not found"));
    }

    private SupportTicketDto toDto(SupportTicket t) {
        return new SupportTicketDto(
                t.getId(), t.getRaisedBy().getId(), t.getRaisedBy().getFullName(),
                t.getRelatedCase() != null ? t.getRelatedCase().getId() : null,
                t.getRelatedCase() != null ? t.getRelatedCase().getCaseNumber() : null,
                t.getSubject(), t.getStatus().name(), t.getCreatedAt()
        );
    }

    private SupportTicketMessageDto toDto(SupportTicketMessage m) {
        return new SupportTicketMessageDto(
                m.getId(), m.getTicket().getId(), m.getSender().getId(), m.getSender().getFullName(),
                m.getBody(), m.getCreatedAt()
        );
    }
}