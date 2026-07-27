package com.taxin60sec.backend.workflow;

import com.taxin60sec.backend.entity.enums.WorkflowStage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkflowTransitionValidator {

    private final WorkflowStateMachine workflowStateMachine;

    public void validate(
            WorkflowStage from,
            WorkflowStage to
    ) {

        if (from == to) {
            return;
        }

        if (!workflowStateMachine.canTransition(from, to)) {

            throw new InvalidWorkflowTransitionException(
                    "Illegal workflow transition: "
                            + from
                            + " -> "
                            + to
            );

        }

    }

}