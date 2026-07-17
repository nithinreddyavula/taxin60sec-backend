package com.taxin60sec.backend.ai;

import com.taxin60sec.backend.document.DocumentAnalysisResult;
import com.taxin60sec.backend.entity.UploadedDocument;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AiDocumentValidatorImpl implements AiDocumentValidator {

    @Override
    public DocumentAnalysisResult validate(UploadedDocument document) {

        DocumentAnalysisResult result = new DocumentAnalysisResult();

        result.setValid(true);
        result.setConfidence(0.95);
        result.setDocumentType(document.getDocumentType());
        result.setSummary("AI validation placeholder.");

        result.setIssues(new ArrayList<>());

        return result;
    }
}