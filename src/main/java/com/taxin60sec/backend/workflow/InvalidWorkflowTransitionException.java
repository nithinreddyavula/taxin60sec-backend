package com.taxin60sec.backend.workflow;

public class InvalidWorkflowTransitionException extends RuntimeException {

    public InvalidWorkflowTransitionException(String message) {
        super(message);
    }

}