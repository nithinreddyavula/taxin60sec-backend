package com.taxin60sec.backend.conversation;

public interface ConversationService {

    ConversationSession startConversation(Long caseId);

    String getCurrentQuestion(Long caseId);

    ConversationSession submitAnswer(Long caseId, String answer);

    boolean isConversationCompleted(Long caseId);

    String generateSummary(Long caseId);
}