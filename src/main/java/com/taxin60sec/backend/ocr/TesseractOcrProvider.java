package com.taxin60sec.backend.ocr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Component
public class TesseractOcrProvider implements OcrProvider {
    private final String executable;
    public TesseractOcrProvider(@Value("${ocr.tesseract.executable:tesseract}") String executable) { this.executable = executable; }
    public String name() { return "tesseract"; }
    public boolean supports(String mimeType) { return mimeType != null && mimeType.startsWith("image/"); }
    public OcrResult extract(Path file, String mimeType, int pageCount) { try { Process p = new ProcessBuilder(executable, file.toString(), "stdout", "--psm", "6").redirectErrorStream(true).start(); if (!p.waitFor(30, TimeUnit.SECONDS) || p.exitValue() != 0) return OcrResult.empty(name(), pageCount); String text = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8); return new OcrResult(name(), text, text.isBlank() ? 0D : .75D, OcrFieldExtractor.fields(text), List.of(), pageCount); } catch (Exception ignored) { return OcrResult.empty(name(), pageCount); } }
}
