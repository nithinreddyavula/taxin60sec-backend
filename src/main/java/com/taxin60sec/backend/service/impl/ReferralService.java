package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.referral.ReferralInfoResponse;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Every client gets a referral code. Codes are generated lazily via ensureReferralCode()
 * rather than only at signup time, so existing users created before this feature shipped
 * self-heal the first time anything asks for their code — no backfill migration needed.
 */
@Service
@Transactional
public class ReferralService {

    // no 0/O/1/I - avoids confusion when a code is read aloud or typed from a screenshot
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final String publicUrl;

    public ReferralService(UserRepository userRepository, @Value("${app.public-url}") String publicUrl) {
        this.userRepository = userRepository;
        this.publicUrl = publicUrl;
    }

    public User ensureReferralCode(User user) {
        if (user.getReferralCode() != null && !user.getReferralCode().isBlank()) {
            return user;
        }
        String code;
        do {
            code = generateCode();
        } while (userRepository.existsByReferralCode(code));

        user.setReferralCode(code);
        return userRepository.save(user);
    }

    public String codeFor(User user) {
        return ensureReferralCode(user).getReferralCode();
    }

    public long referralCountFor(String referralCode) {
        return userRepository.countByReferredByCode(referralCode);
    }

    public ReferralInfoResponse getInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        user = ensureReferralCode(user);

        long referredCount = userRepository.countByReferredByCode(user.getReferralCode());

        return new ReferralInfoResponse(
                user.getReferralCode(),
                publicUrl + "/?ref=" + user.getReferralCode(),
                referredCount
        );
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}