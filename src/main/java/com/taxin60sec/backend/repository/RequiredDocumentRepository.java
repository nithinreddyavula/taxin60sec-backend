package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.RequiredDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequiredDocumentRepository extends JpaRepository<RequiredDocument, Long> {
    List<RequiredDocument> findByServiceOfferingIdAndDeletedFalseOrderByDisplayOrderAsc(Long serviceId);
    List<RequiredDocument> findByTaxCaseIdAndDeletedFalseOrderByDisplayOrderAsc(Long caseId);
}
