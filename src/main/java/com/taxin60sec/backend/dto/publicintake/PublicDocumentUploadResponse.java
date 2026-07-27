package com.taxin60sec.backend.dto.publicintake;

public record PublicDocumentUploadResponse(

        Long documentId,

        String fileName,

        String status,

        String message

) {}