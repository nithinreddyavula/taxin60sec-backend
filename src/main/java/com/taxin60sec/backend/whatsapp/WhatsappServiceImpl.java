package com.taxin60sec.backend.whatsapp;

import com.taxin60sec.backend.ai.AiConversationService;
import com.taxin60sec.backend.ai.AiResponse;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import com.taxin60sec.backend.identity.CaseResolverService;
import com.taxin60sec.backend.workflow.CaseIntelligenceService;
import com.taxin60sec.backend.workflow.WorkflowService;
import com.taxin60sec.backend.whatsapp.WhatsappFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import com.taxin60sec.backend.document.DocumentUploadRequest;
import com.taxin60sec.backend.entity.RequiredDocument;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WhatsappServiceImpl implements WhatsappService {

    private final AiConversationService aiConversationService;
    private final CaseResolverService caseResolverService;
    private final WhatsappFlowService whatsappFlowService;

    private final RestClient restClient;

@Value("${whatsapp.phone-number-id}")
private String phoneNumberId;

@Value("${whatsapp.access-token}")
private String accessToken;

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    @Override
    public String verifyWebhook(
            String mode,
            String token,
            String challenge
    ) {

        if ("subscribe".equals(mode)
                && verifyToken.equals(token)) {

            return challenge;
        }

        throw new RuntimeException("Invalid webhook verification.");
    }

    public String processMessage(WhatsappMessage message) {
        System.out.println("processMessage called");    

    System.out.println("Processing: " + message);

    Long caseId = caseResolverService.resolveCaseId(
            message.getPhoneNumber()
    );

    System.out.println("Case ID = " + caseId);

    String reply = whatsappFlowService.handleMessage(
            caseId,
            message
    );

    System.out.println("Reply = " + reply);

    sendTextMessage(
            message.getPhoneNumber(),
            reply
    );

    System.out.println("Reply sent");

    return reply;
}

    @Override
public void sendTextMessage(String phoneNumber, String text) {

    try {
        String url = "https://graph.facebook.com/v23.0/"
                + phoneNumberId
                + "/messages";

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", phoneNumber,
                "type", "text",
                "text", Map.of("body", text)
        );

        restClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .toBodilessEntity();

                System.out.println("Sending reply to: " + phoneNumber);

        System.out.println("Message sent successfully.");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}