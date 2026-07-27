package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.healthscore.TaxHealthCheckRequest;
import com.taxin60sec.backend.dto.healthscore.TaxHealthCheckResponse;
import com.taxin60sec.backend.service.TaxHealthCheckService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public, no-auth endpoints for the free Tax Health Score tool.
 * Deliberately outside /api/v1/admin and /api/v1/auth — this is meant to be
 * hit by anonymous visitors from a marketing page, not existing clients.
 */
@RestController
@RequestMapping("/api/v1/health-score")
public class TaxHealthCheckController {

    private final TaxHealthCheckService service;

    public TaxHealthCheckController(TaxHealthCheckService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<TaxHealthCheckResponse>> check(
            @Valid @RequestBody TaxHealthCheckRequest request,
            HttpServletRequest httpRequest
    ) {
        TaxHealthCheckResponse response = service.submit(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Tax Health Score calculated", response, httpRequest.getRequestURI())
        );
    }

    @GetMapping("/{shareToken}")
    public ApiResponse<TaxHealthCheckResponse> getResult(
            @PathVariable String shareToken,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                "Tax Health Score result",
                service.getByShareToken(shareToken),
                httpRequest.getRequestURI()
        );
    }
}
