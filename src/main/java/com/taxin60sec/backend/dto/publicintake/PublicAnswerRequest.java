package com.taxin60sec.backend.dto.publicintake;

import jakarta.validation.constraints.NotBlank;

public record PublicAnswerRequest(

        @NotBlank
        String question,

        @NotBlank
        String answer

) {
}