package com.taxin60sec.backend.jobs;

import com.taxin60sec.backend.entity.DeadlineSubscriber;
import com.taxin60sec.backend.entity.enums.ComplianceType;
import com.taxin60sec.backend.repository.DeadlineSubscriberRepository;
import com.taxin60sec.backend.service.NotificationService;
import com.taxin60sec.backend.utils.DeadlineCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Sends a monthly WhatsApp digest of upcoming tax deadlines to every subscriber who
 * opted in via the homepage widget - the recurring touchpoint that gives non-clients
 * a reason to stay engaged with Tax60 every month, not just once a year.
 */
@Component
public class DeadlineDigestJob {

    private static final Logger log = LoggerFactory.getLogger(DeadlineDigestJob.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM");

    private static final Map<ComplianceType, String> TITLES = Map.of(
            ComplianceType.GST_RETURN, "GSTR-3B",
            ComplianceType.TDS_RETURN, "TDS Return",
            ComplianceType.ADVANCE_TAX, "Advance Tax",
            ComplianceType.ITR_FILING, "ITR",
            ComplianceType.ROC_FILING, "ROC Filing"
    );

    private final DeadlineSubscriberRepository subscriberRepository;
    private final NotificationService notificationService;

    public DeadlineDigestJob(
            DeadlineSubscriberRepository subscriberRepository,
            NotificationService notificationService
    ) {
        this.subscriberRepository = subscriberRepository;
        this.notificationService = notificationService;
    }

    // Runs at 9am on the 1st of every month.
    @Scheduled(cron = "0 0 9 1 * *")
    public void run() {

        LocalDate today = LocalDate.now();

        String summary = TITLES.entrySet().stream()
                .map(e -> e.getValue() + ": " + DeadlineCalculator.nextDueDate(e.getKey(), today).format(DATE_FORMAT))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        List<DeadlineSubscriber> subscribers = subscriberRepository.findByActiveTrue();

        int sent = 0;
        for (DeadlineSubscriber subscriber : subscribers) {
            try {
                notificationService.sendMonthlyDeadlineDigestWhatsApp(subscriber.getPhoneNumber(), summary);
                sent++;
            } catch (Exception ex) {
                log.warn("Failed to send monthly deadline digest to subscriber {}", subscriber.getId(), ex);
            }
        }

        log.info("Monthly deadline digest sent to {}/{} subscribers", sent, subscribers.size());
    }
}