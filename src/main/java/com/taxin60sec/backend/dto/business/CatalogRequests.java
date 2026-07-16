package com.taxin60sec.backend.dto.business;

import com.taxin60sec.backend.entity.enums.ServiceCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class CatalogRequests {
    private CatalogRequests() { }
    public record Service(@NotBlank @Size(max=80) String code, @NotBlank @Size(max=160) String displayName,
                          @Size(max=1200) String description, @NotNull ServiceCategory category, @Min(0) Integer estimatedCompletionDays,
                          @DecimalMin("0.00") BigDecimal basePrice, @DecimalMin("0.00") BigDecimal minimumPrice,
                          @DecimalMin("0.00") BigDecimal maximumPrice, @Min(0) Integer displayOrder, String icon, String color,
                          Boolean requiresPaymentFirst, Boolean requiresDocumentVerification) { }
    public record RequiredDocument(@NotBlank @Size(max=160) String name, @NotBlank @Size(max=120) String documentType,
                                   @Size(max=600) String description, Boolean mandatory, String acceptedFileTypes,
                                   @Min(1) Long maximumFileSize, String sampleDocumentUrl, @Min(0) Integer displayOrder) { }
    public record Reorder(@NotEmpty List<@NotNull Long> documentIds) { }
}
