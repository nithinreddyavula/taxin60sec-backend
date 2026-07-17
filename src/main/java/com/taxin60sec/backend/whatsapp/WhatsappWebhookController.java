package com.taxin60sec.backend.whatsapp;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/whatsapp")
@RequiredArgsConstructor
public class WhatsappWebhookController {

    private final WhatsappService whatsappService;

    @PostMapping
    public ResponseEntity<String> receiveMessage(
            @RequestBody WhatsappMessage message) {

        return ResponseEntity.ok(
                whatsappService.processMessage(message)
        );
    }
}