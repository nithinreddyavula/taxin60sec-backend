package com.taxin60sec.backend.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAnalysisResult {

    private boolean valid;

    private double confidence;

    private String documentType;

    private List<String> issues;

    private String summary;

    private int pageCount;
    private boolean duplicate;
    private boolean corrupted;
    private boolean encrypted;
    private boolean ownershipValidated;
    private Map<String, String> extractedFields = Map.of();
}
