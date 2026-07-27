package com.taxin60sec.backend.workflow;

import com.taxin60sec.backend.entity.enums.WorkflowStage;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class WorkflowStateMachine {

    private final Map<WorkflowStage, Set<WorkflowStage>> transitions =
            new EnumMap<>(WorkflowStage.class);

    public WorkflowStateMachine() {

        transitions.put(
                WorkflowStage.CREATED,
                EnumSet.of(
                        WorkflowStage.DOCUMENTS_PENDING,
                        WorkflowStage.CANCELLED
                )
        );

        transitions.put(
                WorkflowStage.DOCUMENTS_PENDING,
                EnumSet.of(
                        WorkflowStage.DOCUMENTS_UPLOADED,
                        WorkflowStage.CANCELLED
                )
        );

        transitions.put(
                WorkflowStage.DOCUMENTS_UPLOADED,
                EnumSet.of(
                        WorkflowStage.UNDER_REVIEW,
                        WorkflowStage.CLIENT_ACTION_REQUIRED,
                        WorkflowStage.CANCELLED
                )
        );

        transitions.put(
                WorkflowStage.CLIENT_ACTION_REQUIRED,
                EnumSet.of(
                        WorkflowStage.DOCUMENTS_PENDING,
                        WorkflowStage.DOCUMENTS_UPLOADED,
                        WorkflowStage.CANCELLED
                )
        );

        transitions.put(
                WorkflowStage.UNDER_REVIEW,
                EnumSet.of(
                        WorkflowStage.PAYMENT_PENDING,
                        WorkflowStage.PROCESSING,
                        WorkflowStage.CLIENT_ACTION_REQUIRED,
                        WorkflowStage.CANCELLED
                )
        );

        transitions.put(
                WorkflowStage.PAYMENT_PENDING,
                EnumSet.of(
                        WorkflowStage.PROCESSING,
                        WorkflowStage.CANCELLED
                )
        );

        transitions.put(
                WorkflowStage.PROCESSING,
                EnumSet.of(
                        WorkflowStage.COMPLETED,
                        WorkflowStage.CANCELLED
                )
        );

        transitions.put(
                WorkflowStage.COMPLETED,
                EnumSet.noneOf(WorkflowStage.class)
        );

        transitions.put(
                WorkflowStage.CANCELLED,
                EnumSet.noneOf(WorkflowStage.class)
        );

    }

    public boolean canTransition(
            WorkflowStage from,
            WorkflowStage to
    ) {

        return transitions
                .getOrDefault(from, EnumSet.noneOf(WorkflowStage.class))
                .contains(to);

    }

}