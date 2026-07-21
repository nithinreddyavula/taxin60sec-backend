package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.RequiredDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ServiceOffering;
import java.util.List;
import java.util.Optional;

public interface RequiredDocumentRepository
        extends JpaRepository<RequiredDocument, Long> {

    List<RequiredDocument> findByServiceOfferingOrderByDisplayOrder(
            ServiceOffering serviceOffering
    );

    List<RequiredDocument> findByTaxCaseOrderByDisplayOrder(
            Case taxCase
    );

}