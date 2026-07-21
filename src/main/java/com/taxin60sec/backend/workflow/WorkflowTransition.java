package com.taxin60sec.backend.workflow;

import com.taxin60sec.backend.entity.enums.WorkflowStage;

public record WorkflowTransition(
        WorkflowStage from,
        WorkflowStage to
) {}