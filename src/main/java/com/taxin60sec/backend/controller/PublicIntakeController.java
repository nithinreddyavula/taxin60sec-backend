package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.publicintake.PublicStartRequest;
import com.taxin60sec.backend.dto.publicintake.PublicStartResponse;
import com.taxin60sec.backend.service.PublicIntakeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.taxin60sec.backend.dto.publicintake.PublicAnswerRequest;
import com.taxin60sec.backend.dto.publicintake.PublicAnswerResponse;
import com.taxin60sec.backend.dto.publicintake.NextAnswerRequest;
import com.taxin60sec.backend.dto.publicintake.ResumeIntakeResponse;
import com.taxin60sec.backend.dto.publicintake.RequiredDocumentResponse;
import com.taxin60sec.backend.document.DocumentValidationResult;

@RestController
@RequestMapping("/api/v1/public/intake")
public class PublicIntakeController {

    private final PublicIntakeService service;

    public PublicIntakeController(
            PublicIntakeService service
    ) {
        this.service = service;
    }

    @PostMapping("/start")
    public ApiResponse<PublicStartResponse> start(

            @Valid
            @RequestBody
            PublicStartRequest request

    ) {

        return ApiResponse.success(

                "Case created successfully",

                service.start(request),

                null

        );

    }
    @PostMapping("/cases/{caseId}/answers")
public ApiResponse<PublicAnswerResponse> saveAnswer(

        @PathVariable Long caseId,

        @Valid
        @RequestBody PublicAnswerRequest request

) {

    return ApiResponse.success(

            "Answer saved",

            service.saveAnswer(caseId, request),

            null

    );

}
@PostMapping("/cases/{caseId}/next")
public ApiResponse<PublicAnswerResponse> next(

        @PathVariable Long caseId,

        @Valid
        @RequestBody NextAnswerRequest request

) {

    return ApiResponse.success(

            "Answer saved",

            service.next(caseId, request),

            null

    );

}
@GetMapping("/resume/{token}")
public ApiResponse<ResumeIntakeResponse> resume(
        @PathVariable String token
){

    return ApiResponse.success(

            "Resume loaded",

            service.resume(token),

            null

    );

}
@GetMapping("/cases/{caseId}/documents/required")
public ApiResponse<List<RequiredDocumentResponse>> requiredDocuments(
        @PathVariable Long caseId
) {

    return ApiResponse.success(

            "Required documents",

            service.getRequiredDocuments(caseId),

            null

    );

}
@GetMapping("/cases/{caseId}/documents")
public ApiResponse<List<RequiredDocumentResponse>> documents(
        @PathVariable Long caseId
) {

    return ApiResponse.success(

            "Required documents",

            service.getRequiredDocuments(caseId),

            null

    );

}
@PostMapping(
        value = "/cases/{caseId}/documents",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
)
public ApiResponse<Void> upload(

        @PathVariable Long caseId,

        @RequestParam Long requiredDocumentId,

        @RequestPart MultipartFile file

) {

    service.uploadDocument(

            caseId,

            requiredDocumentId,

            file

    );

    return ApiResponse.success(

            "Document uploaded",

            null,

            null

    );

}
@GetMapping("/cases/{caseId}/documents/validate")
public ApiResponse<DocumentValidationResult> validate(

        @PathVariable Long caseId

) {

    return ApiResponse.success(

            "Validation",

            service.validateDocuments(caseId),

            null

    );

}
@PostMapping("/cases/{caseId}/submit")
public ApiResponse<Void> submit(

        @PathVariable Long caseId

) {

    service.submitCase(caseId);

    return ApiResponse.success(

            "Application submitted successfully",

            null,

            null

    );

}
}