package com.taxin60sec.backend.jobs;

import com.taxin60sec.backend.entity.ComplianceObligation;
import com.taxin60sec.backend.entity.enums.ComplianceStatus;
import com.taxin60sec.backend.entity.enums.NoticeSeverity;
import com.taxin60sec.backend.entity.enums.NoticeType;
import com.taxin60sec.backend.repository.ComplianceObligationRepository;
import com.taxin60sec.backend.service.NotificationService;
import com.taxin60sec.backend.service.impl.NoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ComplianceReminderJob {

    private static final Logger log = LoggerFactory.getLogger(ComplianceReminderJob.class);
    private static final int REMINDER_WINDOW_DAYS = 5;
    private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final ComplianceObligationRepository obligations;
    private final NotificationService notificationService;
    private final NoticeService noticeService;

    public ComplianceReminderJob(ComplianceObligationRepository obligations, NotificationService notificationService, NoticeService noticeService) {
        this.obligations = obligations;
        this.notificationService = notificationService;
        this.noticeService = noticeService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void run() {
        LocalDate today = LocalDate.now();
        markOverdue(today);
        sendUpcomingReminders(today);
    }

    private void markOverdue(LocalDate today) {
        List<ComplianceObligation> overdue =
                obligations.findByStatusAndDeletedFalseAndDueDateBefore(ComplianceStatus.PENDING, today);
        for (ComplianceObligation o : overdue) {
            o.setStatus(ComplianceStatus.OVERDUE);

            if (o.getClient() != null) {
                try {
                    noticeService.create(
                            o.getClient(),
                            NoticeType.DEADLINE,
                            NoticeSeverity.ACTION_REQUIRED,
                            o.getTitle() + " is now overdue",
                            "This was due on " + o.getDueDate().format(DUE_DATE_FORMAT) + ". Fix it as soon as possible to avoid penalties.",
                            o.getRelatedCase()
                    );
                } catch (Exception ex) {
                    log.warn("Failed to create overdue notice for obligation {}", o.getId(), ex);
                }
            }
        }
        if (!overdue.isEmpty()) {
            log.info("Compliance job: flipped {} obligation(s) to OVERDUE", overdue.size());
        }
    }

    private void sendUpcomingReminders(LocalDate today) {
        LocalDate windowEnd = today.plusDays(REMINDER_WINDOW_DAYS);
        List<ComplianceObligation> dueSoon =
                obligations.findByStatusAndDeletedFalseAndDueDateBetween(ComplianceStatus.PENDING, today, windowEnd);

        int sent = 0;
        for (ComplianceObligation o : dueSoon) {
            if (o.isReminderSent()) continue;

            var client = o.getClient();
            if (client == null) continue;

            String dueDate = o.getDueDate().format(DUE_DATE_FORMAT);

            if (client.getEmail() != null && !client.getEmail().isBlank()) {
                try {
                    notificationService.sendComplianceReminderEmail(client.getEmail(), client.getFullName(), o.getTitle(), dueDate);
                } catch (Exception ex) {
                    log.warn("Failed to send compliance reminder email for obligation {}", o.getId(), ex);
                }
            }
            if (client.getPhoneNumber() != null && !client.getPhoneNumber().isBlank()) {
                try {
                    notificationService.sendComplianceReminderWhatsApp(client.getPhoneNumber(), client.getFullName(), o.getTitle(), dueDate);
                } catch (Exception ex) {
                    log.warn("Failed to send compliance reminder WhatsApp for obligation {}", o.getId(), ex);
                }
            }

            try {
                noticeService.create(
                        client,
                        NoticeType.DEADLINE,
                        NoticeSeverity.WARNING,
                        o.getTitle() + " is due soon",
                        "Due on " + dueDate + ". We've sent you a reminder - let us know if you'd like help with it.",
                        o.getRelatedCase()
                );
            } catch (Exception ex) {
                log.warn("Failed to create due-soon notice for obligation {}", o.getId(), ex);
            }

            o.setReminderSent(true);
            sent++;
        }
        if (sent > 0) {
            log.info("Compliance job: sent {} deadline reminder(s)", sent);
        }
    }
}