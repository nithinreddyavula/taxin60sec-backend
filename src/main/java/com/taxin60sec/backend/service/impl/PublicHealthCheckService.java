package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.healthcheck.HealthCheckRequest;
import com.taxin60sec.backend.dto.healthcheck.HealthCheckResponse;
import com.taxin60sec.backend.dto.healthcheck.HealthCheckResponse.CategoryStatus;
import com.taxin60sec.backend.dto.healthcheck.HealthCheckResponse.Issue;
import com.taxin60sec.backend.dto.healthcheck.HealthCheckResponse.Recommendation;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.repository.ServiceOfferingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PublicHealthCheckService {

    private record Rule(String question, boolean triggerWhenFalse, String issueTitle, String severity, String recommendedCode, String category) {}

    private static final Map<String, List<Rule>> RULES = Map.of(

            "INDIVIDUAL", List.of(
                    new Rule("itr_filed", true, "Income Tax Return not filed for this year", "HIGH", "INCOME_TAX", "Income Tax"),
                    new Rule("advance_tax_paid", true, "Advance tax may be pending", "MEDIUM", "INCOME_TAX", "Income Tax")
            ),

            "FREELANCER", List.of(
                    new Rule("itr_filed", true, "Income Tax Return not filed for this year", "HIGH", "INCOME_TAX", "Income Tax"),
                    new Rule("advance_tax_paid", true, "Advance tax may be pending", "MEDIUM", "INCOME_TAX", "Income Tax"),
                    new Rule("gst_registered", true, "GST registration may be required for your income level", "MEDIUM", "GST_FILING", "GST")
            ),

            "BUSINESS", List.of(
                    new Rule("gst_filed", true, "GST returns not filed for this period", "HIGH", "GST_FILING", "GST"),
                    new Rule("tds_filed", true, "TDS compliance needs review", "MEDIUM", "VIRTUAL_CFO", "TDS"),
                    new Rule("roc_filed", true, "ROC annual filing is pending", "HIGH", "STARTUP_SERVICES", "ROC")
            ),

            "NRI", List.of(
                    new Rule("itr_filed", true, "Indian Income Tax Return not filed for this year", "HIGH", "NRI_TAXATION", "Income Tax"),
                    new Rule("accounts_declared", true, "NRE/NRO accounts not declared", "MEDIUM", "NRI_TAXATION", "NRI Compliance"),
                    new Rule("repatriation_forms", true, "Fund repatriation may be missing Form 15CA/15CB", "HIGH", "NRI_TAXATION", "NRI Compliance")
            )
    );

    private static final List<String> ALL_CATEGORIES = List.of("Income Tax", "GST", "TDS", "ROC", "NRI Compliance", "Payroll");

    private final ServiceOfferingRepository serviceOfferingRepository;

    public PublicHealthCheckService(ServiceOfferingRepository serviceOfferingRepository) {
        this.serviceOfferingRepository = serviceOfferingRepository;
    }

    public HealthCheckResponse evaluate(HealthCheckRequest request) {

        String userType = request.userType() == null ? "INDIVIDUAL" : request.userType().toUpperCase();
        Map<String, Boolean> answers = request.answers() == null ? Map.of() : request.answers();
        List<Rule> rules = RULES.getOrDefault(userType, RULES.get("INDIVIDUAL"));

        int score = 100;
        List<Issue> issues = new ArrayList<>();
        Map<String, Rule> triggeredByCode = new LinkedHashMap<>();

        Set<String> categoriesInPersona = new LinkedHashSet<>();
        Map<String, String> worstSeverityByCategory = new LinkedHashMap<>();
        Map<String, String> detailByCategory = new LinkedHashMap<>();

        for (Rule rule : rules) {
            categoriesInPersona.add(rule.category());

            Boolean answer = answers.get(rule.question());
            boolean triggered = rule.triggerWhenFalse()
                    ? Boolean.FALSE.equals(answer) || answer == null
                    : Boolean.TRUE.equals(answer);

            if (!triggered) continue;

            issues.add(new Issue(rule.issueTitle(), rule.severity()));
            score -= "HIGH".equals(rule.severity()) ? 20 : 10;
            triggeredByCode.putIfAbsent(rule.recommendedCode(), rule);

            String current = worstSeverityByCategory.get(rule.category());
            if (current == null || ("MEDIUM".equals(current) && "HIGH".equals(rule.severity()))) {
                worstSeverityByCategory.put(rule.category(), rule.severity());
                detailByCategory.put(rule.category(), rule.issueTitle());
            }
        }

        score = Math.max(0, Math.min(100, score));

        String statusLabel;
        if (score >= 85) statusLabel = "Healthy";
        else if (score >= 60) statusLabel = "Needs attention";
        else statusLabel = "At risk";

        List<Recommendation> recommendations = triggeredByCode.keySet().stream()
                .map(this::toRecommendation)
                .filter(r -> r != null)
                .toList();

        List<CategoryStatus> categories = ALL_CATEGORIES.stream()
                .map(category -> {
                    if (!categoriesInPersona.contains(category)) {
                        return new CategoryStatus(category, "NOT_APPLICABLE", null);
                    }
                    String severity = worstSeverityByCategory.get(category);
                    if (severity == null) {
                        return new CategoryStatus(category, "HEALTHY", null);
                    }
                    String status = "HIGH".equals(severity) ? "CRITICAL" : "ATTENTION";
                    return new CategoryStatus(category, status, detailByCategory.get(category));
                })
                .toList();

        return new HealthCheckResponse(score, statusLabel, issues, recommendations, categories);
    }

    private Recommendation toRecommendation(String code) {

        return serviceOfferingRepository.findAll().stream()
                .filter(s -> code.equals(s.getCode()) && s.isActive())
                .findFirst()
                .map(this::toRecommendationDto)
                .orElse(null);
    }

    private Recommendation toRecommendationDto(ServiceOffering offering) {
        return new Recommendation(
                offering.getId(),
                offering.getCode(),
                offering.getDisplayName(),
                offering.getBasePrice(),
                offering.getEstimatedCompletionDays()
        );
    }
}