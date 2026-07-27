package com.taxin60sec.backend.whatsapp;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.taxin60sec.backend.whatsapp.dto.MetaWebhookRequest;
import com.taxin60sec.backend.whatsapp.WhatsappService;

@RestController
@RequestMapping("/api/webhooks/whatsapp")
@RequiredArgsConstructor
public class WhatsappWebhookController {

    private final WhatsappService whatsappService;
    private final WhatsappWebhookMapper webhookMapper;

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
                System.out.println("Webhook received");

        return ResponseEntity.ok(
                whatsappService.verifyWebhook(
                        mode,
                        token,
                        challenge
                )
        );
    }

    @PostMapping
public ResponseEntity<String> receiveMessage(
        @RequestBody MetaWebhookRequest request) {

    System.out.println("Webhook received");

    WhatsappMessage message = webhookMapper.toWhatsappMessage(request);

    System.out.println("Mapped message: " + message);

    if (message != null) {
        whatsappService.processMessage(message);
    }

    return ResponseEntity.ok("EVENT_RECEIVED");
}
}