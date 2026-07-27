package com.taxin60sec.backend.document;

import java.util.Locale;

public enum DocumentType {
    PAN, AADHAAR, GST_CERTIFICATE, BANK_STATEMENT, CANCELLED_CHEQUE, MOA, AOA,
    LLP_AGREEMENT, PARTNERSHIP_DEED, FINANCIAL_STATEMENTS, IT_RETURNS, GST_RETURNS,
    INVOICE, SALARY_SLIP, RENTAL_AGREEMENT, MSME, FSSAI, IEC, PASSPORT, DRIVING_LICENCE,
    OTHER;
    public static DocumentType from(String value) {
        if (value == null || value.isBlank()) return OTHER;
        try { return valueOf(value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_")); }
        catch (IllegalArgumentException ignored) { return OTHER; }
    }
}
