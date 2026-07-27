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
}