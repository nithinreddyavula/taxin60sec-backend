package com.taxin60sec.backend.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
}