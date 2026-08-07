package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.business.CaApplicationRequest;
import com.taxin60sec.backend.dto.domain.CAProfileDto;
import com.taxin60sec.backend.dto.domain.CAPublicProfileDto;
import com.taxin60sec.backend.entity.CAProfile;
import com.taxin60sec.backend.entity.Role;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.BackgroundCheckStatus;
import com.taxin60sec.backend.entity.enums.CAAvailability;
import com.taxin60sec.backend.entity.enums.CATier;
import com.taxin60sec.backend.entity.enums.CaseStatus;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.CAProfileRepository;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.RoleRepository;
import com.taxin60sec.backend.repository.UserRepository;
import com.taxin60sec.backend.service.CAProfileService;
import com.taxin60sec.backend.storage.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class CAProfileServiceImpl implements CAProfileService {

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of("PRACTICE_CERTIFICATE", "PAN_CARD");
    private static final Set<CaseStatus> TERMINAL_STATUSES = EnumSet.of(CaseStatus.COMPLETED, CaseStatus.CANCELLED);

    private final UserRepository users;
    private final RoleRepository roles;
    private final CAProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final CaseRepository cases;

    public CAProfileServiceImpl(UserRepository users, RoleRepository roles, CAProfileRepository profiles, PasswordEncoder passwordEncoder, StorageService storageService, CaseRepository cases) {
        this.users = users;
        this.roles = roles;
        this.profiles = profiles;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
        this.cases = cases;
    }

    @Override
    public CAProfileDto apply(CaApplicationRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "Email already belongs to another account.");
        }

        Role caRole = roles.findByName("ROLE_CA")
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "ROLE_CA not found"));

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.getRoles().add(caRole);
        user = users.save(user);

        CAProfile profile = new CAProfile();
        profile.setUser(user);
        profile.setMembershipNumber(request.membershipNumber());
        profile.setPanNumber(request.panNumber());
        profile.setFirmName(request.firmName());
        profile.setSpecialization(request.specialization());
        profile.setVerified(false);

        return toDto(profiles.save(profile));
    }

    @Override
    public List<CAProfileDto> pendingApplications() {
        return profiles.findByVerifiedFalseOrderByCreatedAtAsc().stream().map(this::toDto).toList();
    }

    @Override
    public List<CAProfileDto> verifiedCAs() {
        return profiles.findByVerifiedTrueOrderByCreatedAtAsc().stream().map(this::toDto).toList();
    }

    @Override
    public CAProfileDto verify(Long profileId) {
        CAProfile profile = profileById(profileId);

        if (!profile.documentsComplete()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "Cannot verify - practice certificate and PAN document are not both uploaded yet");
        }
        if (profile.getBackgroundCheckStatus() != BackgroundCheckStatus.PASSED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "Cannot verify - background check has not passed yet");
        }
        if (profile.getAgreementAcceptedAt() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "Cannot verify - partner agreement has not been accepted yet");
        }
        if (profile.getTier() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "Cannot verify - assign a tier (Junior/Senior) first");
        }

        profile.setVerified(true);
        return toDto(profile);
    }

    @Override
    public void reject(Long profileId) {
        CAProfile profile = profileById(profileId);
        profile.markDeleted();
    }

    @Override
    public CAProfileDto myProfile(Long userId) {
        return profiles.findByUserId(userId)
                .map(this::toDto)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "CA profile not found"));
    }

    @Override
    public CAPublicProfileDto publicProfile(Long caUserId) {
        CAProfile profile = profiles.findByUserId(caUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "CA profile not found"));

        return new CAPublicProfileDto(
                profile.getUser().getId(),
                profile.getUser().getFullName(),
                profile.getMembershipNumber(),
                profile.getFirmName(),
                profile.getSpecialization(),
                profile.isVerified(),
                profile.getTier() != null ? profile.getTier().name() : null
        );
    }

    @Override
    public CAProfileDto uploadDocument(Long userId, String documentType, MultipartFile file) {
        String type = documentType == null ? "" : documentType.toUpperCase();
        if (!ALLOWED_DOCUMENT_TYPES.contains(type)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "documentType must be one of " + ALLOWED_DOCUMENT_TYPES);
        }

        CAProfile profile = profiles.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "CA profile not found"));

        StorageService.StorageMetadata metadata = storageService.upload(file, true);

        if ("PRACTICE_CERTIFICATE".equals(type)) {
            profile.setPracticeCertificateKey(metadata.key());
            profile.setPracticeCertificateVersion(metadata.version());
        } else {
            profile.setPanDocumentKey(metadata.key());
            profile.setPanDocumentVersion(metadata.version());
        }

        return toDto(profile);
    }

    @Override
    public CAProfileDto setTier(Long profileId, CATier tier) {
        CAProfile profile = profileById(profileId);
        profile.setTier(tier);
        return toDto(profile);
    }

    @Override
    public CAProfileDto setBackgroundCheckStatus(Long profileId, BackgroundCheckStatus status) {
        CAProfile profile = profileById(profileId);
        profile.setBackgroundCheckStatus(status);
        return toDto(profile);
    }

    @Override
    public CAProfileDto acceptAgreement(Long userId, String agreementVersion) {
        CAProfile profile = profiles.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "CA profile not found"));
        profile.setAgreementAcceptedAt(Instant.now());
        profile.setAgreementVersion(agreementVersion);
        return toDto(profile);
    }

    @Override
    public CAProfileDto setAvailability(Long userId, CAAvailability availability) {
        CAProfile profile = profiles.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "CA profile not found"));
        profile.setAvailability(availability);
        return toDto(profile);
    }

    @Override
    public CAProfileDto setPayoutDestination(Long userId, com.taxin60sec.backend.dto.business.PayoutDestinationRequest request) {
        CAProfile profile = profiles.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "CA profile not found"));

        if (request.method() == com.taxin60sec.backend.entity.enums.PayoutMethod.UPI) {
            if (request.upiId() == null || request.upiId().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "upiId is required for UPI payouts");
            }
        } else {
            if (request.bankAccountNumber() == null || request.bankAccountNumber().isBlank()
                    || request.bankIfsc() == null || request.bankIfsc().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "bankAccountNumber and bankIfsc are required for bank transfer payouts");
            }
        }
        if (request.accountHolderName() == null || request.accountHolderName().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "accountHolderName is required");
        }

        boolean changed = !Objects.equals(profile.getPayoutMethod(), request.method())
                || !Objects.equals(profile.getPayoutBankAccountNumber(), request.bankAccountNumber())
                || !Objects.equals(profile.getPayoutUpiId(), request.upiId());
        if (changed) {
            profile.setPayoutProviderFundAccountId(null);
        }

        profile.setPayoutMethod(request.method());
        profile.setPayoutAccountHolderName(request.accountHolderName());
        profile.setPayoutBankAccountNumber(request.bankAccountNumber());
        profile.setPayoutBankIfsc(request.bankIfsc());
        profile.setPayoutUpiId(request.upiId());

        return toDto(profile);
    }

    private CAProfile profileById(Long id) {
        return profiles.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "CA application not found"));
    }

    private CAProfileDto toDto(CAProfile profile) {
        long activeCaseload = cases.countByAssignedCa_IdAndDeletedFalseAndStatusNotIn(profile.getUser().getId(), TERMINAL_STATUSES);
        return new CAProfileDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                profile.getMembershipNumber(),
                profile.getPanNumber(),
                profile.getFirmName(),
                profile.getSpecialization(),
                profile.isVerified(),
                profile.getTier() != null ? profile.getTier().name() : null,
                profile.getBackgroundCheckStatus().name(),
                profile.getPracticeCertificateKey() != null,
                profile.getPanDocumentKey() != null,
                profile.getAgreementAcceptedAt() != null,
                profile.getAvailability().name(),
                activeCaseload,
                profile.payoutDestinationConfigured(),
                profile.getPayoutMethod() != null ? profile.getPayoutMethod().name() : null,
                profile.getPayoutUpiId(),
                maskAccountNumber(profile.getPayoutBankAccountNumber())
        );
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return null;
        return "•".repeat(Math.max(0, accountNumber.length() - 4)) + accountNumber.substring(accountNumber.length() - 4);
    }
}