package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.ai.AiConversationService;
import com.taxin60sec.backend.ai.AiResponse;
import lombok.RequiredArgsConstructor;
import com.taxin60sec.backend.ai.ChatRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiConversationService aiConversationService;

    @PostMapping("/chat/{caseId}")
    public ResponseEntity<AiResponse> chat(
            @PathVariable Long caseId,
            @RequestBody ChatRequest request) {

        return ResponseEntity.ok(
                aiConversationService.chat(caseId, request.getMessage())
        );
    }
}