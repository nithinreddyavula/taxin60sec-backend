package com.taxin60sec.backend.utils;

import com.taxin60sec.backend.entity.enums.ComplianceType;

import java.time.LocalDate;

public final class DeadlineCalculator {

    private DeadlineCalculator() {}

    public static LocalDate nextDueDate(ComplianceType type, LocalDate today) {
        return switch (type) {
            case GST_RETURN -> today.withDayOfMonth(1).plusMonths(1).withDayOfMonth(20);
            case TDS_RETURN -> nextQuarterlyTdsDueDate(today);
            case ADVANCE_TAX -> nextAdvanceTaxDueDate(today);
            case ITR_FILING -> onOrAfter(today, LocalDate.of(today.getYear(), 7, 31));
            case ROC_FILING -> onOrAfter(today, LocalDate.of(today.getYear(), 11, 30));
            case OTHER -> today.plusDays(30);
        };
    }

    private static LocalDate nextQuarterlyTdsDueDate(LocalDate today) {
        for (int month : new int[]{7, 10, 1, 5}) {
            LocalDate candidate = month == 1 || month == 5
                    ? LocalDate.of(today.getYear() + (month < today.getMonthValue() ? 1 : 0), month, 7)
                    : LocalDate.of(today.getYear(), month, 7);
            if (!candidate.isBefore(today)) return candidate;
        }
        return LocalDate.of(today.getYear() + 1, 7, 7);
    }

    private static LocalDate nextAdvanceTaxDueDate(LocalDate today) {
        for (int month : new int[]{6, 9, 12, 3}) {
            int year = month == 3 && today.getMonthValue() > 3 ? today.getYear() + 1 : today.getYear();
            LocalDate candidate = LocalDate.of(year, month, 15);
            if (!candidate.isBefore(today)) return candidate;
        }
        return LocalDate.of(today.getYear() + 1, 6, 15);
    }

    private static LocalDate onOrAfter(LocalDate today, LocalDate thisYearDeadline) {
        return today.isAfter(thisYearDeadline) ? thisYearDeadline.plusYears(1) : thisYearDeadline;
    }
}