package com.taxin60sec.backend.whatsapp;

public interface WhatsappService {

    String verifyWebhook(
            String mode,
            String verifyToken,
            String challenge
    );

    String processMessage(
            WhatsappMessage message
    );

    void sendTextMessage(
            String phoneNumber,
            String text
    );
}