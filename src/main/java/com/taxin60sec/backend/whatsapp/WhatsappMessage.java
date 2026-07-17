package com.taxin60sec.backend.whatsapp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhatsappMessage {

    private String phoneNumber;

    private String message;
}