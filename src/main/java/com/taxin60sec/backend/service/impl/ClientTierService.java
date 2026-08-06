package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.dto.business.ClientTierResponse;
import com.taxin60sec.backend.entity.ClientProfile;
import com.taxin60sec.backend.entity.enums.CasePriority;
import com.taxin60sec.backend.entity.enums.ClientTier;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.repository.ClientProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientTierService {

    private final ClientProfileRepository clientProfiles;

    public ClientTierService(ClientProfileRepository clientProfiles) {
        this.clientProfiles = clientProfiles;
    }

    public ClientTierResponse tierForUser(Long userId) {
        ClientTier tier = clientProfiles.findByUserId(userId)
                .map(ClientProfile::getTier)
                .orElse(ClientTier.STANDARD);

        return toResponse(tier);
    }

    /** Best-effort lookup used by case creation — never blocks case creation if no profile exists yet. */
    public ClientTier tierForUserOrDefault(Long userId) {
        return clientProfiles.findByUserId(userId)
                .map(ClientProfile::getTier)
                .orElse(ClientTier.STANDARD);
    }

    public CasePriority defaultPriorityFor(ClientTier tier) {
        return switch (tier) {
            case VIP -> CasePriority.URGENT;
            case PRIORITY -> CasePriority.HIGH;
            case STANDARD -> CasePriority.NORMAL;
        };
    }

    /** Staff-only: set a client's tier directly (e.g. after an offline upsell conversation). */
    public ClientTierResponse updateTier(Long clientProfileId, ClientTier tier) {
        ClientProfile profile = clientProfiles.findById(clientProfileId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Client profile not found"));

        profile.setTier(tier);
        return toResponse(tier);
    }

    private ClientTierResponse toResponse(ClientTier tier) {
        return new ClientTierResponse(tier, labelFor(tier), perksFor(tier), defaultPriorityFor(tier));
    }

    private String labelFor(ClientTier tier) {
        return switch (tier) {
            case VIP -> "VIP";
            case PRIORITY -> "Priority";
            case STANDARD -> "Standard";
        };
    }

    private List<String> perksFor(ClientTier tier) {
        return switch (tier) {
            case VIP -> List.of(
                    "Urgent priority on every case",
                    "Dedicated CA point of contact",
                    "Same-day document review"
            );
            case PRIORITY -> List.of(
                    "High priority on every case",
                    "Faster document review turnaround"
            );
            case STANDARD -> List.of(
                    "Standard case handling"
            );
        };
    }
}