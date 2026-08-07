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

    void sendComplianceReminderEmail(
            String email,
            String name,
            String obligationTitle,
            String dueDate
    );

    void sendComplianceReminderWhatsApp(
            String phoneNumber,
            String name,
            String obligationTitle,
            String dueDate
    );

    void sendInstantAcknowledgmentEmail(
            String email,
            String name,
            String serviceName
    );

    void sendInstantAcknowledgmentWhatsApp(
            String phoneNumber,
            String name,
            String serviceName
    );

    void sendHealthCheckResultsEmail(
            String email,
            int score,
            String statusLabel,
            String issuesSummary
    );

    void sendCaAssignmentEmail(
            String caEmail,
            String caName,
            String caseNumber,
            String serviceName,
            String clientName
    );

    void sendMonthlyDeadlineDigestWhatsApp(
            String phoneNumber,
            String deadlinesSummary
    );

    void sendNewCaseMessageEmail(
            String recipientEmail,
            String recipientName,
            String caseNumber,
            String senderName,
            String messagePreview
    );

    /** WhatsApp counterpart to sendCaAssignmentEmail - the CA gets both channels, same as every other event in this service. */
    void sendCaAssignmentWhatsApp(
            String caPhone,
            String caName,
            String caseNumber
    );

    /** The client-side gap: previously only the CA was told a case had been assigned. */
    void sendClientCaAssignedEmail(
            String clientEmail,
            String clientName,
            String caseNumber,
            String serviceName,
            String caName
    );

    void sendClientCaAssignedWhatsApp(
            String clientPhone,
            String clientName,
            String caseNumber
    );

}