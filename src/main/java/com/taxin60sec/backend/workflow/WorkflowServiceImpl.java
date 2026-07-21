package com.taxin60sec.backend.workflow;

import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.TimelineEvent;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.entity.enums.ConversationState;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.TimelineEventRepository;
import com.taxin60sec.backend.workflow.WorkflowStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowServiceImpl implements WorkflowService {

    private final CaseRepository caseRepository;
    private final TimelineEventRepository timelineRepository;
    private final WorkflowTransitionValidator validator;
    private final WorkflowStateMachine workflowStateMachine;

    @Override
    public Case transition(
            Long caseId,
            WorkflowStage targetStage,
            String reason,
            User actor
    ) {

        Case taxCase = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new RuntimeException("Case not found"));

        WorkflowStage current = taxCase.getWorkflowStage();

        validator.validate(current, targetStage);

        taxCase.setWorkflowStage(targetStage);

        synchronizeStates(taxCase, targetStage);

        createTimeline(
                taxCase,
                current,
                targetStage,
                reason,
                actor
        );

        caseRepository.save(taxCase);

return taxCase;
    }

    @Override
public void logEvent(
        Case taxCase,
        User actor,
        String action,
        String description,
        Map<String, Object> metadata
) {

    TimelineEvent event = new TimelineEvent();

    event.setTaxCase(taxCase);
    event.setActor(actor);
    event.setEventType(action);
    event.setTitle(action);
    event.setDescription(description);

    timelineRepository.save(event);
}

    @Override
    public boolean canTransition(
            WorkflowStage from,
            WorkflowStage to
    ) {
        return workflowStateMachine.canTransition(from, to);
    }

    private void synchronizeStates(
            Case taxCase,
            WorkflowStage stage
    ) {

        switch (stage) {

            case CREATED -> {
                taxCase.setStatus(CaseStatus.DRAFT);
                taxCase.setConversationState(
                        ConversationState.GREETING);
            }

            case DOCUMENTS_PENDING -> {
                taxCase.setStatus(
                        CaseStatus.DOCUMENT_COLLECTION);
                taxCase.setConversationState(
                        ConversationState.COLLECTING_DOCUMENTS);
            }

            case DOCUMENTS_UPLOADED -> {
                taxCase.setConversationState(
                        ConversationState.READY_FOR_CA);
            }

            case UNDER_REVIEW -> {
                taxCase.setStatus(
                        CaseStatus.CA_REVIEW);
                taxCase.setConversationState(
                        ConversationState.COMPLETED);
            }

            case CLIENT_ACTION_REQUIRED -> {
                taxCase.setStatus(
                        CaseStatus.DOCUMENT_COLLECTION);
                taxCase.setConversationState(
                        ConversationState.COLLECTING_DOCUMENTS);
            }

            case PAYMENT_PENDING -> {
                taxCase.setStatus(
                        CaseStatus.INTAKE);
                taxCase.setPaymentRequired(true);
                taxCase.setConversationState(
                        ConversationState.WAITING_FOR_PAYMENT);
            }

            case PROCESSING -> {
                taxCase.setStatus(
                        CaseStatus.IN_PROGRESS);
            }

            case COMPLETED -> {

                taxCase.setStatus(
                        CaseStatus.COMPLETED);

                taxCase.setCompletedAt(
                        Instant.now());

                taxCase.setConversationState(
                        ConversationState.COMPLETED);
            }

            case CANCELLED -> {
                taxCase.setStatus(
                        CaseStatus.CANCELLED);
            }

        }

    }

    private void createTimeline(
            Case taxCase,
            WorkflowStage from,
            WorkflowStage to,
            String reason,
            User actor
    ) {

        TimelineEvent event = new TimelineEvent();

        event.setTaxCase(taxCase);

        event.setActor(actor);

        event.setEventType("WORKFLOW");

        event.setTitle(
                from + " → " + to);

        event.setDescription(reason);

        timelineRepository.save(event);

    }

}