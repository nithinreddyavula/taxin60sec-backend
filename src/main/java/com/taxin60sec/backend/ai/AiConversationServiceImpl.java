package com.taxin60sec.backend.ai;

import com.taxin60sec.backend.conversation.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl implements AiConversationService {

    private final ConversationService conversationService;

    @Override
    public AiResponse chat(Long caseId, String message) {

        if (!conversationService.isConversationCompleted(caseId)) {

            conversationService.submitAnswer(caseId, message);

            String nextQuestion = conversationService.getCurrentQuestion(caseId);

            if (nextQuestion == null) {
                return new AiResponse(
                        conversationService.generateSummary(caseId),
                        true
                );
            }

            return new AiResponse(nextQuestion, false);
        }

        return new AiResponse(
                conversationService.generateSummary(caseId),
                true
        );
    }
}