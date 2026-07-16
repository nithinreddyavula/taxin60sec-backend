package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.UploadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {
    List<UploadedDocument> findByTaxCaseIdAndDeletedFalseOrderByCreatedAtDesc(Long caseId);
}
