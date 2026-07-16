package com.taxin60sec.backend.workflow;

public interface WorkflowService {
    void startCaseWorkflow(Long caseId);

    void advanceCaseWorkflow(Long caseId, String transition);
}
