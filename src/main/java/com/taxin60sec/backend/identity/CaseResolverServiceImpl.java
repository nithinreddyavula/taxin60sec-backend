package com.taxin60sec.backend.identity;

import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaseResolverServiceImpl implements CaseResolverService {

    private final UserRepository userRepository;
    private final CaseRepository caseRepository;

    @Override
    public Long resolveCaseId(String phoneNumber) {

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() ->
                        new RuntimeException("User not found for phone number: " + phoneNumber));

        Case taxCase = caseRepository
                .findFirstByClientIdAndArchivedFalseAndDeletedFalseOrderByUpdatedAtDesc(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("No active case found for user"));

        return taxCase.getId();
    }
}