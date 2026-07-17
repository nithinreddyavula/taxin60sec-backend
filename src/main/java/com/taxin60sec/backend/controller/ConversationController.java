package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.conversation.AnswerRequest;
import com.taxin60sec.backend.conversation.ConversationService;
import com.taxin60sec.backend.conversation.ConversationSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/{caseId}/start")
    public ResponseEntity<ConversationSession> startConversation(
            @PathVariable Long caseId) {

        return ResponseEntity.ok(
                conversationService.startConversation(caseId)
        );
    }

    @GetMapping("/{caseId}/question")
    public ResponseEntity<String> getCurrentQuestion(
            @PathVariable Long caseId) {

        return ResponseEntity.ok(
                conversationService.getCurrentQuestion(caseId)
        );
    }

    @PostMapping("/{caseId}/answer")
    public ResponseEntity<ConversationSession> submitAnswer(
            @PathVariable Long caseId,
            @RequestBody AnswerRequest request) {

        return ResponseEntity.ok(
                conversationService.submitAnswer(caseId, request.getAnswer())
        );
    }

    @GetMapping("/{caseId}/summary")
    public ResponseEntity<String> getSummary(
            @PathVariable Long caseId) {

        return ResponseEntity.ok(
                conversationService.generateSummary(caseId)
        );
    }

    @GetMapping("/{caseId}/completed")
    public ResponseEntity<Boolean> isCompleted(
            @PathVariable Long caseId) {

        return ResponseEntity.ok(
                conversationService.isConversationCompleted(caseId)
        );
    }
}