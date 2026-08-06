package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.dto.referral.ReferralInfoResponse;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.UserRepository;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.ReferralService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/referrals")
public class ReferralController {

    private final ReferralService referralService;
    private final UserRepository userRepository;

    @Value("${app.public-url}")
    private String publicUrl;

    public ReferralController(ReferralService referralService, UserRepository userRepository) {
        this.referralService = referralService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @Transactional
    public ApiResponse<ReferralInfoResponse> myReferralInfo(@AuthenticationPrincipal UserPrincipal principal) {

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        ApiErrorCode.NOT_FOUND,
                        "User not found"
                ));

        String code = referralService.codeFor(user);
        long total = referralService.referralCountFor(code);
        String link = publicUrl + "?ref=" + code;

        return ApiResponse.success(
                "Referral info",
                new ReferralInfoResponse(code, link, total),
                null
        );
    }
}