package com.taxin60sec.backend.ai.provider;

import org.springframework.stereotype.Component;

@Component
public class OpenRouterAiProvider implements AiProvider {

    @Override
    public String generate(String prompt) {

        throw new UnsupportedOperationException(
                "OpenRouter integration not implemented yet"
        );

    }
}