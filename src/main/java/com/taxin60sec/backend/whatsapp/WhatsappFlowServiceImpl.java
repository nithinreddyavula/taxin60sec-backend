package com.taxin60sec.backend.whatsapp;

import com.taxin60sec.backend.ai.AiConversationService;
import com.taxin60sec.backend.ai.AiResponse;
import com.taxin60sec.backend.workflow.CaseIntelligenceService;
import lombok.RequiredArgsConstructor;
import com.taxin60sec.backend.whatsapp.WhatsappDocumentService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WhatsappFlowServiceImpl implements WhatsappFlowService {

    private final AiConversationService aiConversationService;
    private final CaseIntelligenceService caseIntelligenceService;
    private final WhatsappDocumentService whatsappDocumentService;

    @Override
    public String handleMessage(Long caseId,
                                WhatsappMessage message) {

        // 1. Document message
        if (message.isDocument()) {
            return handleDocument(caseId, message);
        }

        // 2. Greeting
        if (isGreeting(message.getMessage())) {
            return """
                    👋 Welcome to TaxIn60Sec!

                    I'm here to help you with tax and compliance services.

                    How can I help you today?
                    """;
        }

        // 3. Status query
        if (isStatusQuery(message.getMessage())) {

            var checklist = caseIntelligenceService.checklist(caseId);

            if (checklist.missing().isEmpty()) {
                return "✅ All required documents have been received.";
            }

            StringBuilder builder = new StringBuilder();
            builder.append("📄 Remaining documents:\n\n");

            checklist.missing()
                    .forEach(doc ->
                            builder.append("• ")
                                   .append(doc)
                                   .append("\n"));

            return builder.toString();
        }

        // 4. AI fallback
        AiResponse response = aiConversationService.chat(
                caseId,
                message.getMessage()
        );

        return response.getReply();
    }

    private String handleDocument(Long caseId,
                              WhatsappMessage message) {

    return whatsappDocumentService.processDocument(
            caseId,
            message
    );
}

    private boolean isGreeting(String text) {

        if (text == null) {
            return false;
        }

        text = text.toLowerCase();

        return text.equals("hi")
                || text.equals("hello")
                || text.equals("hey");
    }

    private boolean isStatusQuery(String text) {

        if (text == null) {
            return false;
        }

        text = text.toLowerCase();

        return text.contains("status")
                || text.contains("documents")
                || text.contains("pending");
    }
}