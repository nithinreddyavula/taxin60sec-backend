package com.taxin60sec.backend.whatsapp.dto;

import lombok.Data;

@Data
public class Message {

    private String from;

    private String type;

    private Text text;

    private Document document;

    private Image image;

}