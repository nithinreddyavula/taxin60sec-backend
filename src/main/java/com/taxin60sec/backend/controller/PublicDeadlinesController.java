package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.deadlines.DeadlineSubscribeRequest;
import com.taxin60sec.backend.dto.deadlines.DeadlinesResponse;
import com.taxin60sec.backend.dto.deadlines.DeadlinesResponse.DeadlineItem;
import com.taxin60sec.backend.entity.DeadlineSubscriber;
import com.taxin60sec.backend.entity.enums.ComplianceType;
import com.taxin60sec.backend.repository.DeadlineSubscriberRepository;
import com.taxin60sec.backend.utils.DeadlineCalculator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/intake/deadlines")
public class PublicDeadlinesController {

    private static final java.util.Map<ComplianceType, String> TITLES = java.util.Map.of(
            ComplianceType.GST_RETURN, "GSTR-3B Filing",
            ComplianceType.TDS_RETURN, "TDS Return Filing",
            ComplianceType.ADVANCE_TAX, "Advance Tax Payment",
            ComplianceType.ITR_FILING, "Income Tax Return Filing",
            ComplianceType.ROC_FILING, "ROC Annual Filing"
    );

    private final DeadlineSubscriberRepository subscriberRepository;

    public PublicDeadlinesController(DeadlineSubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    @GetMapping
    public ApiResponse<DeadlinesResponse> upcoming() {

        LocalDate today = LocalDate.now();

        List<DeadlineItem> items = TITLES.entrySet().stream()
                .map(entry -> {
                    LocalDate due = DeadlineCalculator.nextDueDate(entry.getKey(), today);
                    long daysRemaining = ChronoUnit.DAYS.between(today, due);
                    return new DeadlineItem(entry.getKey().name(), entry.getValue(), due, (int) daysRemaining);
                })
                .sorted((a, b) -> a.dueDate().compareTo(b.dueDate()))
                .toList();

        return ApiResponse.success("Upcoming deadlines", new DeadlinesResponse(items), null);
    }

    @PostMapping("/subscribe")
    public ApiResponse<Void> subscribe(@RequestBody DeadlineSubscribeRequest request) {

        String phone = request.phoneNumber() == null ? "" : request.phoneNumber().trim();
        if (phone.isEmpty()) {
            return ApiResponse.error("Phone number is required", null, null);
        }

        DeadlineSubscriber subscriber = subscriberRepository.findByPhoneNumber(phone)
                .orElseGet(DeadlineSubscriber::new);

        subscriber.setPhoneNumber(phone);
        subscriber.setActive(true);

        subscriberRepository.save(subscriber);

        return ApiResponse.success("Subscribed", null, null);
    }
}