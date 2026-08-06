package com.taxin60sec.backend.dto.business;

import com.taxin60sec.backend.entity.enums.PayoutMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PayoutDestinationRequest(
        @NotNull PayoutMethod method,
        @Size(max = 120) String accountHolderName,
        @Size(max = 34) String bankAccountNumber,
        @Size(max = 11) String bankIfsc,
        @Size(max = 100) String upiId
) {
}