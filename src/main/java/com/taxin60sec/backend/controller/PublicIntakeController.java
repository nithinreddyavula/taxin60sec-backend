package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.publicintake.PublicStartRequest;
import com.taxin60sec.backend.dto.publicintake.PublicStartResponse;
import com.taxin60sec.backend.service.PublicIntakeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
}