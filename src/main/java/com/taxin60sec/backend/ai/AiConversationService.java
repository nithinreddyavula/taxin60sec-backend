package com.taxin60sec.backend.ai;

public interface AiConversationService {

    AiResponse chat(Long caseId, String message);

}