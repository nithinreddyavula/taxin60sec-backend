package com.taxin60sec.backend.notification;

public interface NotificationService {
    void send(String recipient, String subject, String message);
}
