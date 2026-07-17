package com.taxin60sec.backend.ocr;
import java.util.*;
import java.util.regex.*;
public final class OcrFieldExtractor {
    private OcrFieldExtractor() { }
    private static final Map<String, Pattern> PATTERNS = Map.of(
            "pan", Pattern.compile("\\b[A-Z]{5}[0-9]{4}[A-Z]\\b"), "gstin", Pattern.compile("\\b[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]Z[0-9A-Z]\\b"),
            "aadhaar", Pattern.compile("\\b[0-9]{4}[ -]?[0-9]{4}[ -]?[0-9]{4}\\b"), "ifsc", Pattern.compile("\\b[A-Z]{4}0[A-Z0-9]{6}\\b"),
            "accountNumber", Pattern.compile("(?i)(?:account(?:\\s*(?:no|number))?\\s*[:#-]?\\s*)([0-9]{9,18})"),
            "invoiceNumber", Pattern.compile("(?i)(?:invoice\\s*(?:no|number)?\\s*[:#-]?\\s*)([A-Z0-9/-]{3,})"),
            "date", Pattern.compile("\\b(?:[0-3]?\\d[/-][01]?\\d[/-](?:19|20)\\d{2}|(?:19|20)\\d{2}[/-][01]?\\d[/-][0-3]?\\d)\\b"),
            "amount", Pattern.compile("(?:₹|INR|Rs\\.?)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE));
    public static Map<String, String> fields(String text) { Map<String, String> values = new LinkedHashMap<>(); PATTERNS.forEach((name, pattern) -> { Matcher m = pattern.matcher(text == null ? "" : text); if (m.find()) values.put(name, m.groupCount() > 0 ? m.group(1) : m.group()); }); return values; }
}
