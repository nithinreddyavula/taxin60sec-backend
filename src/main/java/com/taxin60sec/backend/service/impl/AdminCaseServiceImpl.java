package com.taxin60sec.backend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.dto.admin.AdminCaseSummaryResponse;
import com.taxin60sec.backend.dto.admin.AdminDashboardResponse;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.service.AdminCaseService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminCaseServiceImpl implements AdminCaseService {

    private final CaseRepository caseRepository;
    private final ObjectMapper objectMapper;

    public AdminCaseServiceImpl(
            CaseRepository caseRepository,
            ObjectMapper objectMapper
    ) {
        this.caseRepository = caseRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminDashboardResponse dashboard() {

        return new AdminDashboardResponse(

                caseRepository.count(),

                caseRepository.countByStatus(CaseStatus.DRAFT),

                caseRepository.countByStatus(CaseStatus.INTAKE),

                caseRepository.countByStatus(CaseStatus.DOCUMENT_COLLECTION),

                caseRepository.countByStatus(CaseStatus.CA_REVIEW),

                caseRepository.countByStatus(CaseStatus.IN_PROGRESS),

                caseRepository.countByStatus(CaseStatus.COMPLETED),

                caseRepository.countByStatus(CaseStatus.CANCELLED)

        );

    }

    @Override
    public List<AdminCaseSummaryResponse> getAllCases() {

        return caseRepository

                .findAllByOrderByCreatedAtDesc()

                .stream()

                .map(this::map)

                .toList();

    }

    private AdminCaseSummaryResponse map(Case taxCase) {

        List<String> questions = List.of();

        if (taxCase.getServiceOffering() != null &&
                taxCase.getServiceOffering().getIntakeQuestions() != null &&
                !taxCase.getServiceOffering().getIntakeQuestions().isBlank()) {

            questions = Arrays.stream(

                            taxCase.getServiceOffering()
                                    .getIntakeQuestions()
                                    .split("\\n"))

                    .map(String::trim)

                    .filter(q -> !q.isBlank())

                    .toList();
        }

        Map<String, String> answers = readAnswers(taxCase);

        return new AdminCaseSummaryResponse(

    taxCase.getId(),

    taxCase.getClient() != null
            ? taxCase.getClient().getFullName()
            : "N/A",

    taxCase.getServiceOffering() != null
            ? taxCase.getServiceOffering().getDisplayName()
            : "N/A",

    taxCase.getStatus().name(),

    taxCase.isIntakeCompleted(),

    answers.size(),

    questions.size(),

    taxCase.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
);

    }

    private Map<String, String> readAnswers(Case taxCase) {

        try {

            if (taxCase.getIntakeAnswers() == null ||
                    taxCase.getIntakeAnswers().isBlank()) {

                return new LinkedHashMap<>();

            }

            return objectMapper.readValue(

                    taxCase.getIntakeAnswers(),

                    new TypeReference<LinkedHashMap<String, String>>() {}

            );

        } catch (Exception ex) {

            throw new RuntimeException(ex);

        }

    }
    @Override
public com.taxin60sec.backend.dto.admin.AdminCaseDetailResponse getCase(Long caseId) {
    throw new UnsupportedOperationException("Not implemented yet");
}

@Override
public void updateStatus(Long caseId, String status) {
    throw new UnsupportedOperationException("Not implemented yet");
}

}