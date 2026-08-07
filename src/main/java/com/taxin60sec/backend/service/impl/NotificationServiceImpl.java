package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${mail.from}")
    private String fromEmail;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.api}")
    private String whatsappApi;

    @Value("${admin.notification.email}")
    private String adminEmail;

    @Value("${admin.notification.whatsapp:}")
    private String adminWhatsapp;

    public NotificationServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ---------- existing resume flow (unchanged behaviour) ----------

    @Override
    public void sendResumeEmail(String email, String name, String resumeUrl) {
        sendEmail(
                email,
                "Continue your TaxIn60Sec application",
                "<h2>Hello %s</h2><p>You can continue your application using the link below.</p><a href='%s'>Continue Application</a>"
                        .formatted(name, resumeUrl)
        );
    }

    @Override
    public void sendResumeWhatsApp(String phone, String name, String resumeUrl) {
        sendWhatsAppTemplate(phone, "resume_application", name, resumeUrl);
    }

    // ---------- submission confirmation (to the client) ----------

    @Override
    public void sendSubmissionConfirmationEmail(String email, String name, String caseNumber, String serviceName) {
        sendEmail(
                email,
                "We've received your application - " + caseNumber,
                """
                <h2>Thank you, %s</h2>
                <p>Your application for <strong>%s</strong> (Case Reference <strong>%s</strong>) has been submitted successfully.</p>
                <p>Our CA team will review everything and get back to you shortly.</p>
                """.formatted(name, serviceName, caseNumber)
        );
    }

    @Override
    public void sendSubmissionConfirmationWhatsApp(String phone, String name, String caseNumber) {
        sendWhatsAppTemplate(phone, "application_submitted_client", name, caseNumber);
    }

    // ---------- new-submission alert (to the admin/CA team) ----------

    @Override
    public void sendAdminNewSubmissionEmail(String clientName, String clientEmail, String clientPhone, String serviceName, String caseNumber) {
        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("Skipping admin submission email: admin.notification.email is not configured");
            return;
        }
        sendEmail(
                adminEmail,
                "New submission - " + caseNumber,
                """
                <h2>New case submitted</h2>
                <p><strong>Case:</strong> %s</p>
                <p><strong>Service:</strong> %s</p>
                <p><strong>Client:</strong> %s</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Phone:</strong> %s</p>
                """.formatted(caseNumber, serviceName, clientName, clientEmail == null ? "-" : clientEmail, clientPhone == null ? "-" : clientPhone)
        );
    }

    @Override
    public void sendAdminNewSubmissionWhatsApp(String clientName, String serviceName, String caseNumber) {
        if (adminWhatsapp == null || adminWhatsapp.isBlank()) {
            log.warn("Skipping admin submission WhatsApp message: admin.notification.whatsapp is not configured");
            return;
        }
        sendWhatsAppTemplate(adminWhatsapp, "application_submitted_admin", clientName, serviceName, caseNumber);
    }

    // ---------- compliance deadline reminders ----------

    @Override
    public void sendComplianceReminderEmail(String email, String name, String obligationTitle, String dueDate) {
        sendEmail(
                email,
                "Upcoming deadline: " + obligationTitle,
                """
                <h2>Hi %s</h2>
                <p><strong>%s</strong> is due on <strong>%s</strong>.</p>
                <p>Log in to your Tax60Sec dashboard to check your compliance status, or reply to this email if you need help getting this filed on time.</p>
                """.formatted(name, obligationTitle, dueDate)
        );
    }

    @Override
    public void sendComplianceReminderWhatsApp(String phone, String name, String obligationTitle, String dueDate) {
        sendWhatsAppTemplate(phone, "compliance_deadline_reminder", name, obligationTitle, dueDate);
    }

    // ---------- instant acknowledgment (the 60-second guarantee) ----------

    @Override
    public void sendInstantAcknowledgmentEmail(String email, String name, String serviceName) {
        sendEmail(
                email,
                "Got it, " + name + " - we're on it",
                """
                <h2>Thanks, %s</h2>
                <p>We've received your %s request. A member of our team is already looking at it.</p>
                <p>You'll hear from us shortly with next steps.</p>
                """.formatted(name, serviceName)
        );
    }

    @Override
    public void sendInstantAcknowledgmentWhatsApp(String phone, String name, String serviceName) {
        sendWhatsAppTemplate(phone, "instant_acknowledgment", name, serviceName);
    }

    // ---------- health check lead follow-up ----------

    @Override
    public void sendHealthCheckResultsEmail(String email, int score, String statusLabel, String issuesSummary) {
        sendEmail(
                email,
                "Your Tax Health Score: " + score + "/100",
                """
                <h2>Your tax health score is %d/100 - %s</h2>
                <p>%s</p>
                <p>Ready to fix these? Reply to this email or visit tax60sec.com to get started.</p>
                """.formatted(score, statusLabel, issuesSummary)
        );
    }

    // ---------- shared helpers ----------

    private void sendEmail(String to, String subject, String html) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String escapedHtml = html.replace("\"", "\\\"").replace("\n", "");

        String body = """
        {
          "from":"%s",
          "to":["%s"],
          "subject":"%s",
          "html":"%s"
        }
        """.formatted(fromEmail, to, subject, escapedHtml);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity("https://api.resend.com/emails", request, String.class);
    }

    /**
     * Sends a WhatsApp Cloud API template message with a variable number of body text parameters.
     * IMPORTANT: the template name must already exist and be APPROVED in Meta Business Manager,
     * with the same number of {{n}} placeholders as the number of params passed here, or the
     * WhatsApp API call will fail (Meta rejects unknown/unapproved templates and parameter-count
     * mismatches).
     */
    private void sendWhatsAppTemplate(String phone, String templateName, String... params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) parameters.append(",");
            parameters.append("{\"type\":\"text\",\"text\":\"").append(params[i]).append("\"}");
        }

        String body = """
        {
        "type":"template",
        "messaging_product":"whatsapp",
        "to":"%s",
        "template":{
        "name":"%s",
        "language":{
        "code":"en"
        },
        "components":[
        {
        "type":"body",
        "parameters":[%s]
        }
        ]
        }
        }
        """.formatted(phone, templateName, parameters);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(whatsappApi + "/" + phoneNumberId + "/messages", entity, String.class);
    }
    // ---------- CA assignment ----------

    public void sendCaAssignmentEmail(String caEmail, String caName, String caseNumber, String serviceName, String clientName) {
        sendEmail(
                caEmail,
                "New case assigned: " + caseNumber,
                """
                <h2>Hi %s</h2>
                <p>You've been assigned a new case.</p>
                <ul>
                  <li><strong>Case:</strong> %s</li>
                  <li><strong>Service:</strong> %s</li>
                  <li><strong>Client:</strong> %s</li>
                </ul>
                <p>Log in to your Tax60 dashboard to review the details and get started.</p>
                """.formatted(caName, caseNumber, serviceName, clientName)
        );
    }

    @Override
    public void sendCaAssignmentWhatsApp(String caPhone, String caName, String caseNumber) {
        sendWhatsAppTemplate(caPhone, "new_case_assigned", caName, caseNumber);
    }

    @Override
    public void sendClientCaAssignedEmail(String clientEmail, String clientName, String caseNumber, String serviceName, String caName) {
        sendEmail(
                clientEmail,
                "A Tax60 expert has been assigned to your case: " + caseNumber,
                """
                <h2>Hi %s</h2>
                <p>Good news - a verified Tax60 expert has been assigned to your case and will begin work shortly.</p>
                <ul>
                  <li><strong>Case:</strong> %s</li>
                  <li><strong>Service:</strong> %s</li>
                  <li><strong>Expert:</strong> %s</li>
                </ul>
                <p>Log in to your Tax60 dashboard to track progress and message your expert directly.</p>
                """.formatted(clientName, caseNumber, serviceName, caName)
        );
    }

    @Override
    public void sendClientCaAssignedWhatsApp(String clientPhone, String clientName, String caseNumber) {
        sendWhatsAppTemplate(clientPhone, "client_case_ca_assigned", clientName, caseNumber);
    }

    // ---------- monthly deadline digest (homepage subscriber broadcast) ----------

    @Override
    public void sendMonthlyDeadlineDigestWhatsApp(String phone, String deadlinesSummary) {
        sendWhatsAppTemplate(phone, "monthly_deadline_digest", deadlinesSummary);
    }

    // ---------- in-app chat notification (masked communication layer) ----------

    @Override
    public void sendNewCaseMessageEmail(String recipientEmail, String recipientName, String caseNumber, String senderName, String messagePreview) {
        sendEmail(
                recipientEmail,
                "New message on case " + caseNumber,
                """
                <h2>Hi %s</h2>
                <p><strong>%s</strong> sent you a message on case <strong>%s</strong>:</p>
                <blockquote>%s</blockquote>
                <p>Reply from within your Tax60 dashboard - all case communication stays in-app.</p>
                """.formatted(recipientName, senderName, caseNumber, messagePreview)
        );
    }
}