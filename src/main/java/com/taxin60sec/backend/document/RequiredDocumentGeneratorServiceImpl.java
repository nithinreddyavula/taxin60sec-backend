package com.taxin60sec.backend.document;

import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.RequiredDocument;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.RequiredDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RequiredDocumentGeneratorServiceImpl
        implements RequiredDocumentGeneratorService {

    private final CaseRepository caseRepository;
    private final RequiredDocumentRepository requiredDocumentRepository;

    @Override
    public void generateForCase(Long caseId) {

        Case taxCase = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new RuntimeException("Case not found"));

        if (taxCase.getServiceOffering() == null) {
            return;
        }

        /*
         * Already generated?
         */
        List<RequiredDocument> existing =
                requiredDocumentRepository
                        .findByTaxCaseIdAndDeletedFalseOrderByDisplayOrderAsc(caseId);

        if (!existing.isEmpty()) {
            return;
        }

        /*
         * Templates
         */
        List<RequiredDocument> templates =
                requiredDocumentRepository
                        .findByServiceOfferingIdAndDeletedFalseOrderByDisplayOrderAsc(
                                taxCase.getServiceOffering().getId());

        for (RequiredDocument template : templates) {

            RequiredDocument document = new RequiredDocument();

            document.setTaxCase(taxCase);

            document.setName(template.getName());

            document.setDocumentType(template.getDocumentType());

            document.setDescription(template.getDescription());

            document.setMandatory(template.isMandatory());

            document.setAcceptedFileTypes(template.getAcceptedFileTypes());

            document.setMaximumFileSize(template.getMaximumFileSize());

            document.setSampleDocumentUrl(template.getSampleDocumentUrl());

            document.setDisplayOrder(template.getDisplayOrder());

            requiredDocumentRepository.save(document);
        }

    }

}