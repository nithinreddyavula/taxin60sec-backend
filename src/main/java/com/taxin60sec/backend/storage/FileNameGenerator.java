package com.taxin60sec.backend.storage;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FileNameGenerator {

    public String generate(String originalFilename) {

        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return UUID.randomUUID() + extension;
    }
}