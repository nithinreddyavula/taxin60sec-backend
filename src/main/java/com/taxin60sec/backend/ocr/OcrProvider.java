package com.taxin60sec.backend.ocr;
import java.nio.file.Path;
public interface OcrProvider { String name(); boolean supports(String mimeType); OcrResult extract(Path file, String mimeType, int pageCount); }
