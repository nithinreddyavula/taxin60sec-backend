package com.taxin60sec.backend.document;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentMatcherService {

    String detectDocumentType(MultipartFile file);

}