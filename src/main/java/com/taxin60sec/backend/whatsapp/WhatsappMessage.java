package com.taxin60sec.backend.whatsapp;

import lombok.Data;

@Data
public class WhatsappMessage {

    private String phoneNumber;

    private String message;

    private String mediaId;

    private String fileName;

    private String mimeType;

    public boolean isDocument() {
        return mediaId != null && !mediaId.isBlank();
    }

    public boolean isText() {
        return message != null && !message.isBlank();
    }
}