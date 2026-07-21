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

}