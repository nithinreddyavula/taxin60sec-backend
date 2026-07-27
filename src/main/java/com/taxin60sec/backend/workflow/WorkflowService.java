package com.taxin60sec.backend.workflow;

import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.WorkflowStage;

import java.util.Map;

public interface WorkflowService {

    Case transition(
            Long caseId,
            WorkflowStage targetStage,
            String reason,
            User actor
    );

    boolean canTransition(
            WorkflowStage from,
            WorkflowStage to
    );

    void logEvent(
        Case taxCase,
        User actor,
        String action,
        String description,
        Map<String, Object> metadata
);

}   