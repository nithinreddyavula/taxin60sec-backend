package com.taxin60sec.backend.ai;

import com.taxin60sec.backend.document.DocumentAnalysisResult;
import com.taxin60sec.backend.entity.UploadedDocument;

public interface AiDocumentValidator {

    DocumentAnalysisResult validate(UploadedDocument document);

}