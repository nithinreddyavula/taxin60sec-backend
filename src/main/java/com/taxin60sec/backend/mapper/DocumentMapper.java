package com.taxin60sec.backend.mapper;

import com.taxin60sec.backend.dto.domain.RequiredDocumentDto;
import com.taxin60sec.backend.dto.domain.UploadedDocumentDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.RequiredDocument;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.UploadedDocument;
import com.taxin60sec.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public RequiredDocumentDto toDto(RequiredDocument document) {
        return new RequiredDocumentDto(
                document.getId(),
                document.getName(),
                document.getDocumentType(),
                document.getDescription(),
                document.isMandatory(),
                document.getAcceptedFileTypes(),
                document.getMaximumFileSize(),
                document.getSampleDocumentUrl(),
                document.getDisplayOrder(),
                idOf(document.getServiceOffering()),
                idOf(document.getTaxCase())
        );
    }

    public UploadedDocumentDto toDto(UploadedDocument document) {
        return new UploadedDocumentDto(
                document.getId(),
                document.getOriginalFilename(),
                document.getDocumentType(),
                document.getStorageKey(),
                document.getMimeType(),
                document.getFileSize(),
                document.getVerificationStatus(),
                document.getVerifiedAt(),
                idOf(document.getVerifiedBy()),
                document.getRejectionReason(),
                idOf(document.getTaxCase()),
                document.getRequiredDocument() == null ? null : document.getRequiredDocument().getId(),
                idOf(document.getUploadedBy())
        );
    }

    private Long idOf(Case taxCase) {
        return taxCase == null ? null : taxCase.getId();
    }

    private Long idOf(ServiceOffering serviceOffering) {
        return serviceOffering == null ? null : serviceOffering.getId();
    }

    private Long idOf(User user) {
        return user == null ? null : user.getId();
    }
}
