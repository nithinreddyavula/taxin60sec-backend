package com.taxin60sec.backend.whatsapp;

public interface WhatsappDocumentService {

    String processDocument(Long caseId, WhatsappMessage message);

}