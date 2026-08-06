package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.business.ClientTierResponse;
import com.taxin60sec.backend.dto.business.UpdateTierRequest;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.ClientTierService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClientTierController {

    private final ClientTierService tierService;

    public ClientTierController(ClientTierService tierService) {
        this.tierService = tierService;
    }

    @GetMapping("/api/v1/businesses/me/tier")
    @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')")
    public ApiResponse<ClientTierResponse> myTier(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request) {
        return ApiResponse.success("Tier", tierService.tierForUser(principal.getId()), request.getRequestURI());
    }

    @PutMapping("/api/v1/clients/{clientProfileId}/tier")
    @PreAuthorize("hasAnyRole('CA','ADMIN')")
    public ApiResponse<ClientTierResponse> updateTier(
            @PathVariable Long clientProfileId,
            @Valid @RequestBody UpdateTierRequest body,
            HttpServletRequest request
    ) {
        return ApiResponse.success("Tier updated", tierService.updateTier(clientProfileId, body.tier()), request.getRequestURI());
    }
}