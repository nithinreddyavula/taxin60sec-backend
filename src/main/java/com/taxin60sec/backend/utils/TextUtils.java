package com.taxin60sec.backend.utils;

import java.util.Locale;

public final class TextUtils {
    private TextUtils() {
    }

    public static String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    public static String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
