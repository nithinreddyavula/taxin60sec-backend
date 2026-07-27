package com.taxin60sec.backend.dto.publicintake;

public record RequiredDocumentResponse(

        Long id,

        String name,

        boolean mandatory,

        boolean uploaded

) {}