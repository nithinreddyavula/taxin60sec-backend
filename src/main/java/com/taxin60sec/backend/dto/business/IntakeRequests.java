package com.taxin60sec.backend.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class IntakeRequests {
    private IntakeRequests() { }

    public record Start(@NotNull Long serviceOfferingId, @Size(max = 180) String title) { }

    public record Answer(@NotBlank @Size(max = 500) String question,
                         @NotBlank @Size(max = 2000) String answer) { }

    public record Answers(@NotEmpty List<@NotNull Answer> answers, boolean complete) { }
}
