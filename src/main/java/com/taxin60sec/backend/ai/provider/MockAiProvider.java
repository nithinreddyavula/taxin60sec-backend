package com.taxin60sec.backend.ai.provider;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockAiProvider implements AiProvider {

    @Override
    public String generate(String prompt) {
        return "AI Response: " + prompt;
    }
}