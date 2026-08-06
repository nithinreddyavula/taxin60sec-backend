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

    if (conversationService.isConversationCompleted(caseId)) {
        return new AiResponse(
                conversationService.generateSummary(caseId),
                true
        );
    }

    String currentQuestion = conversationService.getCurrentQuestion(caseId);

    /*
     * FIRST MESSAGE
     */
    if (currentQuestion != null &&
            conversationService.generateSummary(caseId).isBlank()) {

        conversationService.startConversation(caseId);

        return new AiResponse(
                """
                👋 Welcome to Tax60!

                Thank you for choosing our service.

                I'm your AI Tax Assistant.

                I'll ask you a few questions to prepare your case.

                Let's begin.

                %s
                """.formatted(
                        conversationService.getCurrentQuestion(caseId)
                ),
                false
        );
    }

    /*
     * NORMAL FLOW
     */
    conversationService.submitAnswer(caseId, message);

    String nextQuestion = conversationService.getCurrentQuestion(caseId);

    /*
     * ONBOARDING COMPLETED
     */
    if (nextQuestion == null) {

        StringBuilder reply = new StringBuilder();

        reply.append("""
                ✅ Thank you!

                Your onboarding is complete.

                Here is your summary:

                """);

        reply.append(conversationService.generateSummary(caseId));

        reply.append("""

                ------------------------------------

                Please upload the required documents shown in your Tax60 dashboard.

                Once uploaded, I will verify each document automatically before sending your case for CA review.
                """);

        return new AiResponse(reply.toString(), true);
    }

    /*
     * NEXT QUESTION
     */
    return new AiResponse(
            """
            Thanks!

            %s
            """.formatted(nextQuestion),
            false
    );
}
}