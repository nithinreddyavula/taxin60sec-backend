package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationServiceImpl
        implements NotificationService {
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

public NotificationServiceImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
}

    @Override
    public void sendResumeEmail(
            String email,
            String name,
            String resumeUrl
    ) {

        @Override
public void sendResumeEmail(
        String email,
        String name,
        String resumeUrl
) {

    HttpHeaders headers = new HttpHeaders();

    headers.setBearerAuth(apiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    String body = """
    {
      "from":"%s",
      "to":["%s"],
      "subject":"Continue your TaxIn60Sec application",
      "html":"<h2>Hello %s</h2><p>You can continue your application using the link below.</p><a href='%s'>Continue Application</a>"
    }
    """.formatted(
            fromEmail,
            email,
            name,
            resumeUrl
    );

    HttpEntity<String> request =
            new HttpEntity<>(body, headers);

    restTemplate.postForEntity(

            "https://api.resend.com/emails",

            request,

            String.class

    );

}
    }

    @Override
public void sendResumeWhatsApp(

        String phone,

        String name,

        String resumeUrl

) {

    HttpHeaders headers = new HttpHeaders();

    headers.setBearerAuth(accessToken);

    headers.setContentType(MediaType.APPLICATION_JSON);

    String body = """
{
"type":"template",
"messaging_product":"whatsapp",
"to":"%s",
"template":{
"name":"resume_application",
"language":{
"code":"en"
},
"components":[
{
"type":"body",
"parameters":[
{
"type":"text",
"text":"%s"
},
{
"type":"text",
"text":"%s"
}
]
}
]
}
}
""".formatted(

            phone,

            name,

            resumeUrl

    );

    HttpEntity<String> entity =

            new HttpEntity<>(

                    body,

                    headers

            );

    restTemplate.postForEntity(

            whatsappApi +

                    "/" +

                    phoneNumberId +

                    "/messages",

            entity,

            String.class

    );

}

}