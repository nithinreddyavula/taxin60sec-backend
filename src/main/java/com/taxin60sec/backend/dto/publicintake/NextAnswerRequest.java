package com.taxin60sec.backend.dto.publicintake;

import jakarta.validation.constraints.NotBlank;

public record NextAnswerRequest(

        @NotBlank
        String answer

) {}