package com.taxin60sec.backend.ai;

public interface AIService {
    String summarize(String prompt);

    String classify(String prompt);
}
