package com.taxin60sec.backend.service;

public interface NotificationService {

    void sendResumeEmail(
            String email,
            String name,
            String resumeUrl
    );

    void sendResumeWhatsApp(
            String phoneNumber,
            String name,
            String resumeUrl
    );

    void sendSubmissionConfirmationEmail(
            String email,
            String name,
            String caseNumber,
            String serviceName
    );

    void sendSubmissionConfirmationWhatsApp(
            String phoneNumber,
            String name,
            String caseNumber
    );

    void sendAdminNewSubmissionEmail(
            String clientName,
            String clientEmail,
            String clientPhone,
            String serviceName,
            String caseNumber
    );

    void sendAdminNewSubmissionWhatsApp(
            String clientName,
            String serviceName,
            String caseNumber
    );

}