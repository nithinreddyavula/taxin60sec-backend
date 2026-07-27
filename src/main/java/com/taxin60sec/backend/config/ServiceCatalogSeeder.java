package com.taxin60sec.backend.config;

import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.enums.ServiceCategory;
import com.taxin60sec.backend.repository.ServiceOfferingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds new service offerings into the catalog on startup if they don't already exist.
 * There is currently no admin UI for creating services, so new verticals are added here
 * (self-healing, same pattern as the default document-checklist generator) rather than
 * requiring a manual database insert.
 */
@Component
public class ServiceCatalogSeeder implements CommandLineRunner {

    private final ServiceOfferingRepository serviceOfferingRepository;

    public ServiceCatalogSeeder(ServiceOfferingRepository serviceOfferingRepository) {
        this.serviceOfferingRepository = serviceOfferingRepository;
    }

    @Override
    public void run(String... args) {

        seedIfMissing(
                "NRI_TAXATION",
                "NRI Taxation & Advisory",
                "Indian income tax filing, DTAA benefit optimization, NRE/NRO account guidance, and fund repatriation (Form 15CA/15CB) for non-resident Indians.",
                ServiceCategory.NRI,
                7,
                new BigDecimal("4999.00"),
                new BigDecimal("4999.00"),
                new BigDecimal("49999.00"),
                "globe",
                "#0EA5A5",
                new String[]{
                        "Country of residence",
                        "PAN number",
                        "Type of Indian income (rent / capital gains / interest / salary / other)",
                        "NRE or NRO account details"
                }
        );
    }

    private void seedIfMissing(
            String code,
            String displayName,
            String description,
            ServiceCategory category,
            int estimatedCompletionDays,
            BigDecimal basePrice,
            BigDecimal minimumPrice,
            BigDecimal maximumPrice,
            String icon,
            String color,
            String[] intakeQuestions
    ) {

        if (serviceOfferingRepository.findAll().stream()
                .anyMatch(existing -> code.equals(existing.getCode()))) {
            return;
        }

        ServiceOffering offering = new ServiceOffering();
        offering.setCode(code);
        offering.setDisplayName(displayName);
        offering.setDescription(description);
        offering.setCategory(category);
        offering.setEstimatedCompletionDays(estimatedCompletionDays);
        offering.setBasePrice(basePrice);
        offering.setMinimumPrice(minimumPrice);
        offering.setMaximumPrice(maximumPrice);
        offering.setActive(true);
        offering.setFeatured(true);
        offering.setDisplayOrder(0);
        offering.setIcon(icon);
        offering.setColor(color);
        offering.setRequiresPaymentFirst(false);
        offering.setRequiresDocumentVerification(true);
        offering.setIntakeQuestions(String.join("\n", intakeQuestions));

        serviceOfferingRepository.save(offering);
    }
}