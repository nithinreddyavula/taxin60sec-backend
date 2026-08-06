package com.taxin60sec.backend.config;

import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.enums.ServiceCategory;
import com.taxin60sec.backend.repository.ServiceOfferingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds new service offerings into the catalog on startup if they don't already exist.
 * There is currently no admin UI for creating services, so new verticals are added here
 * (self-healing, same pattern as the default document-checklist generator) rather than
 * requiring a manual database insert.
 *
 * A failure seeding any single offering (e.g. a stale DB check constraint rejecting a
 * newly added ServiceCategory value) must never crash application startup - that would
 * take the whole app down over one catalog row. Each seed attempt is isolated and logged
 * instead of thrown, so the app still boots and serves traffic even if catalog seeding
 * for one entry hasn't caught up with the database yet.
 */
@Component
public class ServiceCatalogSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ServiceCatalogSeeder.class);

    private final ServiceOfferingRepository serviceOfferingRepository;

    public ServiceCatalogSeeder(ServiceOfferingRepository serviceOfferingRepository) {
        this.serviceOfferingRepository = serviceOfferingRepository;
    }

    @Override
    public void run(String... args) {

        trySeed(
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

    private void trySeed(
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
        try {
            seedIfMissing(
                    code, displayName, description, category, estimatedCompletionDays,
                    basePrice, minimumPrice, maximumPrice, icon, color, intakeQuestions
            );
        } catch (Exception e) {
            // Never let a single catalog row block application startup - log loudly and
            // move on. Common cause: a DB-level check constraint on service_offerings.category
            // that predates a newly added ServiceCategory enum value. That's a data/schema
            // issue to fix separately; it shouldn't take the whole app down every boot.
            log.error("Failed to seed service offering '{}' - skipping, app will continue starting. "
                    + "Likely cause: a stale DB constraint or schema drift for this table.", code, e);
        }
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