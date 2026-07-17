package com.taxin60sec.backend.ocr;
import java.util.List;
import java.util.Map;
public record OcrResult(String provider, String text, double confidence, Map<String, String> fields,
                        List<List<List<String>>> tables, int pageCount) {
    public static OcrResult empty(String provider, int pageCount) { return new OcrResult(provider, "", 0D, Map.of(), List.of(), pageCount); }
}
