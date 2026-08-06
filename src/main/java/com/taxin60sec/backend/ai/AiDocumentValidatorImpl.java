package com.taxin60sec.backend.ai;

import com.taxin60sec.backend.document.DocumentAnalysisResult;
import com.taxin60sec.backend.entity.UploadedDocument;
import com.taxin60sec.backend.document.DocumentIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AiDocumentValidatorImpl implements AiDocumentValidator {

    private final DocumentIntelligenceService intelligence;

    @Override
    public DocumentAnalysisResult validate(UploadedDocument document) {

        return intelligence.analyze(document);
    }
}
