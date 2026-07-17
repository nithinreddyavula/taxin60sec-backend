package com.taxin60sec.backend.whatsapp;

import com.taxin60sec.backend.ai.AiConversationService;
import com.taxin60sec.backend.ai.AiResponse;
import com.taxin60sec.backend.identity.CaseResolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WhatsappServiceImpl implements WhatsappService {

    private final AiConversationService aiConversationService;
    private final CaseResolverService caseResolverService;

    @Override
    public String processMessage(WhatsappMessage message) {

        Long caseId = caseResolverService.resolveCaseId(
                message.getPhoneNumber()
        );

        AiResponse response = aiConversationService.chat(
                caseId,
                message.getMessage()
        );

        return response.getReply();
    }
}