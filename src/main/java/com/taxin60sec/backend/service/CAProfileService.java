package com.taxin60sec.backend.service;

import com.taxin60sec.backend.dto.business.CaApplicationRequest;
import com.taxin60sec.backend.dto.domain.CAProfileDto;
import com.taxin60sec.backend.entity.enums.BackgroundCheckStatus;
import com.taxin60sec.backend.entity.enums.CAAvailability;
import com.taxin60sec.backend.entity.enums.CATier;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CAProfileService {
    CAProfileDto apply(CaApplicationRequest request);
    List<CAProfileDto> pendingApplications();
    List<CAProfileDto> verifiedCAs();
    CAProfileDto verify(Long profileId);
    void reject(Long profileId);
    CAProfileDto myProfile(Long userId);

    CAProfileDto uploadDocument(Long userId, String documentType, MultipartFile file);
    CAProfileDto setTier(Long profileId, CATier tier);
    CAProfileDto setBackgroundCheckStatus(Long profileId, BackgroundCheckStatus status);
    CAProfileDto acceptAgreement(Long userId, String agreementVersion);

    /** CA self-service - lets a CA mark themselves available/limited/unavailable ahead of assignment. */
    CAProfileDto setAvailability(Long userId, CAAvailability availability);

    /** CA self-service - where their released earnings should actually be sent. */
    CAProfileDto setPayoutDestination(Long userId, com.taxin60sec.backend.dto.business.PayoutDestinationRequest request);
}