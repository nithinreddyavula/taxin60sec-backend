package com.taxin60sec.backend.dto.business;

import com.taxin60sec.backend.entity.enums.CasePriority;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.entity.enums.WorkflowStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public final class CaseRequests {
    private CaseRequests() { }
    public record Create(@NotBlank @Size(max=180) String title, @Size(max=2000) String description,
                         @NotNull Long serviceOfferingId, CasePriority priority, @Size(max=1500) String remarks,
                         LocalDate expectedCompletionDate) { }
    public record Update(@NotBlank @Size(max=180) String title, @Size(max=2000) String description,
                         CasePriority priority, @Size(max=1500) String remarks, LocalDate expectedCompletionDate) { }
    public record Assignment(@NotNull Long caUserId) { }
    public record Stage(@NotNull WorkflowStage stage) { }
    public record Status(@NotNull CaseStatus status) { }
    public record Priority(@NotNull CasePriority priority) { }
    public record Notes(@NotBlank @Size(max=4000) String internalNotes) { }
}
