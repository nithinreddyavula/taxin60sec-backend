package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.UploadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {
}
