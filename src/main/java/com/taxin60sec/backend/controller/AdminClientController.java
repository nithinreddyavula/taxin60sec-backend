package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.dto.admin.AdminClientCaseSummary;
import com.taxin60sec.backend.dto.admin.AdminClientDetailResponse;
import com.taxin60sec.backend.dto.admin.AdminClientSummaryResponse;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ClientProfile;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.ClientProfileRepository;
import com.taxin60sec.backend.repository.UserRepository;
import com.taxin60sec.backend.service.impl.ClientExcelExportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clients")
@PreAuthorize("hasRole('ADMIN')")
public class AdminClientController {

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final CaseRepository caseRepository;
    private final ClientExcelExportService excelExportService;

    public AdminClientController(
            UserRepository userRepository,
            ClientProfileRepository clientProfileRepository,
            CaseRepository caseRepository,
            ClientExcelExportService excelExportService
    ) {
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.caseRepository = caseRepository;
        this.excelExportService = excelExportService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminClientSummaryResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<User> users = userRepository.searchByRole(
                "ROLE_CLIENT",
                (search == null || search.isBlank()) ? null : search.trim(),
                PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size)
        );

        Page<AdminClientSummaryResponse> mapped = users.map(user -> {
            ClientProfile profile = clientProfileRepository.findByUserId(user.getId()).orElse(null);
            return new AdminClientSummaryResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    profile != null ? profile.getPanNumber() : null,
                    user.isActive() ? "Active" : "Inactive",
                    user.getCreatedAt(),
                    caseRepository.countByClientIdAndDeletedFalse(user.getId())
            );
        });

        return ApiResponse.success(
                "Clients loaded",
                new PageResponse<>(mapped.getContent(), mapped.getNumber(), mapped.getSize(), mapped.getTotalElements(), mapped.getTotalPages()),
                null
        );
    }

    /** Full profile for the admin "client details" view - triggered when an admin clicks a client row. */
    @GetMapping("/{id}")
    public ApiResponse<AdminClientDetailResponse> detail(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Client not found"));

        ClientProfile profile = clientProfileRepository.findByUserId(user.getId()).orElse(null);
        List<Case> clientCases = caseRepository.findByClient_IdAndDeletedFalseOrderByCreatedAtDesc(user.getId());

        List<AdminClientCaseSummary> caseSummaries = clientCases.stream()
                .map(c -> new AdminClientCaseSummary(
                        c.getId(),
                        c.getCaseNumber(),
                        c.getServiceOffering() != null ? c.getServiceOffering().getDisplayName() : "N/A",
                        c.getStatus().name(),
                        c.getCreatedAt()
                ))
                .toList();

        AdminClientDetailResponse response = new AdminClientDetailResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.isActive() ? "Active" : "Inactive",
                user.getCreatedAt(),
                profile != null ? profile.getBusinessName() : null,
                profile != null ? profile.getPanNumber() : null,
                profile != null ? profile.getGstin() : null,
                profile != null ? profile.getAddress() : null,
                profile != null && profile.getTier() != null ? profile.getTier().name() : null,
                user.getReferralCode(),
                user.getReferredByCode(),
                user.getReferralCredits(),
                caseSummaries.size(),
                caseSummaries
        );

        return ApiResponse.success("Client detail loaded", response, null);
    }

    /** Streams every client's full profile as a downloadable .xlsx workbook. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        byte[] workbook = excelExportService.exportClients();

        String filename = "tax60-clients-" +
                DateTimeFormatter.ofPattern("yyyy-MM-dd").format(java.time.LocalDate.now(ZoneId.systemDefault())) +
                ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        return new ResponseEntity<>(workbook, headers, HttpStatus.OK);
    }
}