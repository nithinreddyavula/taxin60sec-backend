package com.taxin60sec.backend.document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.taxin60sec.backend.document.DocumentMatcherService;

@Service
@RequiredArgsConstructor
public class DocumentMatcherServiceImpl implements DocumentMatcherService {

    @Override
    public String detectDocumentType(MultipartFile file) {

        String name = file.getOriginalFilename();

        if (name == null) {
            throw new IllegalArgumentException("Filename not found.");
        }

        name = name.toLowerCase();

        if (name.contains("pan")) {
            return "PAN_CARD";
        }

        if (name.contains("aadhaar")
                || name.contains("aadhar")) {
            return "AADHAAR";
        }

        if (name.contains("form16")) {
            return "FORM_16";
        }

        if (name.contains("bank")) {
            return "BANK_STATEMENT";
        }

        throw new IllegalArgumentException(
                "Unable to identify uploaded document."
        );
    }
}