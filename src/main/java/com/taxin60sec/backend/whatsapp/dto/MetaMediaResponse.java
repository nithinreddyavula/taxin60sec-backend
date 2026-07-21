package com.taxin60sec.backend.whatsapp.dto;

import lombok.Data;

@Data
public class MetaMediaResponse {

    private String id;

    private String url;

    private String mime_type;

    private String sha256;

    private Long file_size;

}