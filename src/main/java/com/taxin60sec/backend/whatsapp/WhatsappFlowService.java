package com.taxin60sec.backend.whatsapp;

public interface WhatsappFlowService {

    String handleMessage(Long caseId, WhatsappMessage message);

}