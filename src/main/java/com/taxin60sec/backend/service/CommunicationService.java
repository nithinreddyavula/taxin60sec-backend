package com.taxin60sec.backend.service;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.communication.CallProvider;
import com.taxin60sec.backend.dto.domain.CallSessionDto;
import com.taxin60sec.backend.dto.domain.CaseMessageDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.CallSession;
import com.taxin60sec.backend.entity.CaseMessage;
import com.taxin60sec.backend.entity.Role;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.CallStatus;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.CallSessionRepository;
import com.taxin60sec.backend.repository.CaseMessageRepository;
import com.taxin60sec.backend.repository.CaseRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

/**
 * The masked communication layer: in-app chat and masked-number call requests
 * for a case, so client and CA never need to exchange a direct phone number,
 * email, or WhatsApp contact.
 *
 * Call masking is delegated to a CallProvider bean - ExotelCallProvider when
 * call.provider.exotel.enabled=true and credentials are set, StubCallProvider
 * otherwise. This class doesn't know or care which one is active.
 */
@Service
@Transactional
public class CommunicationService {

    private final CaseRepository cases;
    private final CaseMessageRepository messages;
    private final CallSessionRepository calls;
    private final NotificationService notificationService;
    private final CallProvider callProvider;

    public CommunicationService(
            CaseRepository cases,
            CaseMessageRepository messages,
            CallSessionRepository calls,
            NotificationService notificationService,
            CallProvider callProvider
    ) {
        this.cases = cases;
        this.messages = messages;
        this.calls = calls;
        this.notificationService = notificationService;
        this.callProvider = callProvider;
    }

    // ---------- chat ----------

    public List<CaseMessageDto> listMessages(Long caseId, User actor) {
        return messages.findByCaseRef_IdOrderByCreatedAtAsc(caseId).stream()
                .map(m -> toDto(m, actor))
                .toList();
    }

    public CaseMessageDto sendMessage(Long caseId, String content, User actor) {
        Case c = caseById(caseId);

        CaseMessage message = new CaseMessage();
        message.setCaseRef(c);
        message.setSender(actor);
        message.setSenderRole(primaryRoleOf(actor));
        message.setContent(content);
        CaseMessage saved = messages.save(message);

        notifyOtherParty(c, actor, content);

        return toDto(saved, actor);
    }

    public void markRead(Long caseId, User actor) {
        messages.findByCaseRef_IdOrderByCreatedAtAsc(caseId).stream()
                .filter(m -> m.getReadAt() == null && !m.getSender().getId().equals(actor.getId()))
                .forEach(m -> m.setReadAt(Instant.now()));
    }

    private void notifyOtherParty(Case c, User sender, String content) {
        String preview = content.length() > 160 ? content.substring(0, 160) + "..." : content;

        User recipient = sender.getId().equals(clientId(c)) ? c.getAssignedCa() : c.getClient();
        if (recipient == null || recipient.getId().equals(sender.getId())) return;

        try {
            notificationService.sendNewCaseMessageEmail(
                    recipient.getEmail(), recipient.getFullName(), c.getCaseNumber(), sender.getFullName(), preview
            );
        } catch (Exception e) {
            // Notification failure must never block the message itself from being sent/saved.
        }
    }

    private Long clientId(Case c) {
        return c.getClient() != null ? c.getClient().getId() : null;
    }

    // ---------- calls ----------

    public CallSessionDto requestCall(Long caseId, User actor) {
        Case c = caseById(caseId);

        User other = actor.getId().equals(clientId(c)) ? c.getAssignedCa() : c.getClient();
        if (other == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "No counterpart assigned to this case yet");
        }

        CallSession session = new CallSession();
        session.setCaseRef(c);
        session.setRequestedBy(actor);
        session.setStatus(CallStatus.REQUESTED);
        session.setProvider(callProvider.name());

        CallProvider.ConnectResult result = callProvider.connect(
                new CallProvider.ConnectRequest(actor.getPhoneNumber(), other.getPhoneNumber(), null)
        );

        if (result.success()) {
            session.setMaskedNumber(result.maskedNumber());
            session.setStatus(CallStatus.CONNECTED);
            session.setConnectedAt(Instant.now());
        }

        return toDto(calls.save(session));
    }

    public List<CallSessionDto> callHistory(Long caseId) {
        return calls.findByCaseRef_IdOrderByCreatedAtDesc(caseId).stream().map(this::toDto).toList();
    }

    public CallSessionDto endCall(Long callSessionId, User actor) {
        CallSession session = calls.findById(callSessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Call session not found"));

        session.setStatus(CallStatus.ENDED);
        session.setEndedAt(Instant.now());
        Instant start = session.getConnectedAt() != null ? session.getConnectedAt() : session.getCreatedAt();
        session.setDurationSeconds((int) Duration.between(start, session.getEndedAt()).toSeconds());

        return toDto(session);
    }

    // ---------- shared ----------

    Case caseById(Long id) {
        return cases.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Case not found"));
    }

    private String primaryRoleOf(User user) {
        for (Role role : user.getRoles()) {
            if ("ADMIN".equals(role.getName())) return "ADMIN";
        }
        for (Role role : user.getRoles()) {
            if ("CA".equals(role.getName())) return "CA";
        }
        return "CLIENT";
    }

    private CaseMessageDto toDto(CaseMessage m, User viewer) {
        return new CaseMessageDto(
                m.getId(),
                m.getCaseRef().getId(),
                m.getSender().getId(),
                m.getSender().getFullName(),
                m.getSenderRole(),
                m.getContent(),
                m.getCreatedAt(),
                m.getReadAt() != null || m.getSender().getId().equals(viewer.getId())
        );
    }

    private CallSessionDto toDto(CallSession s) {
        return new CallSessionDto(
                s.getId(),
                s.getCaseRef().getId(),
                s.getRequestedBy().getId(),
                s.getRequestedBy().getFullName(),
                s.getStatus().name(),
                s.getMaskedNumber(),
                s.getProvider(),
                s.getCreatedAt(),
                s.getConnectedAt(),
                s.getEndedAt(),
                s.getDurationSeconds()
        );
    }
}