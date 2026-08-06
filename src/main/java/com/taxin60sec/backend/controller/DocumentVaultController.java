package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.vault.VaultDocumentResponse;
import com.taxin60sec.backend.entity.UploadedDocument;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.UploadedDocumentRepository;
import com.taxin60sec.backend.security.UserPrincipal;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentVaultController {

    private final UploadedDocumentRepository uploadedDocumentRepository;

    public DocumentVaultController(UploadedDocumentRepository uploadedDocumentRepository) {
        this.uploadedDocumentRepository = uploadedDocumentRepository;
    }

    @GetMapping("/vault")
    public ApiResponse<List<VaultDocumentResponse>> myVault(@AuthenticationPrincipal UserPrincipal principal) {

        List<VaultDocumentResponse> documents = uploadedDocumentRepository
                .findByTaxCase_Client_IdAndDeletedFalseOrderByCreatedAtDesc(principal.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success("Vault", documents, null);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        UploadedDocument document = uploadedDocumentRepository.findById(id)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        ApiErrorCode.NOT_FOUND,
                        "Document not found"
                ));

        boolean isOwner = document.getTaxCase() != null
                && document.getTaxCase().getClient() != null
                && Objects.equals(document.getTaxCase().getClient().getId(), principal.getId());

        boolean isStaff = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_CA"));

        if (!isOwner && !isStaff) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "You cannot access this document");
        }

        File file = new File(document.getStorageKey());
        if (!file.exists()) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "File is no longer available");
        }

        Resource resource = new FileSystemResource(file);

        String contentType = document.getMimeType() != null ? document.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalFilename() + "\"")
                .body(resource);
    }

    private VaultDocumentResponse toResponse(UploadedDocument d) {
        return new VaultDocumentResponse(
                d.getId(),
                d.getOriginalFilename(),
                d.getDocumentType(),
                d.getTaxCase() != null ? d.getTaxCase().getCaseNumber() : null,
                d.getTaxCase() != null && d.getTaxCase().getServiceOffering() != null
                        ? d.getTaxCase().getServiceOffering().getDisplayName() : null,
                d.getVerificationStatus().name(),
                d.getFileSize(),
                d.getCreatedAt()
        );
    }
}