package com.taxin60sec.backend.dto.healthscore;

import com.taxin60sec.backend.entity.enums.RevenueBand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * The whole free-check form is deliberately 6 questions — anything longer
 * kills completion rate on a "quick score" tool. No password, no OTP here;
 * phone number is only used to (a) deliver the WhatsApp result and
 * (b) dedupe/attribute referrals.
 */
public record TaxHealthCheckRequest(

        @NotBlank(message = "Name is required")
        String fullName,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
        String phoneNumber,

        String email,

        @NotNull(message = "Please select your annual revenue range")
        RevenueBand revenueBand,

        @NotNull(message = "Please answer whether you are GST registered")
        Boolean gstRegistered,

        LocalDate lastGstFilingDate,

        LocalDate lastItrFilingDate,

        int missingDocumentsCount,

        boolean hasForeignIncome,

        boolean isNri,

        // optional: ?ref=CODE from a shared link
        String referredByCode

) {}
