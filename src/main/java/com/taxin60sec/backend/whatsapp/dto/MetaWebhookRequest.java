package com.taxin60sec.backend.whatsapp.dto;

import lombok.Data;
import java.util.List;

@Data
public class MetaWebhookRequest {

    private String object;

    private List<Entry> entry;

}