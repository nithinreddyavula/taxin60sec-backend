package com.taxin60sec.backend.service;

import com.taxin60sec.backend.dto.business.CaseIntakeResponse;
import com.taxin60sec.backend.dto.business.IntakeRequests;
import com.taxin60sec.backend.entity.User;

public interface CaseIntakeService {
    CaseIntakeResponse startOrResume(IntakeRequests.Start request, User client);
    CaseIntakeResponse recordAnswers(Long caseId, IntakeRequests.Answers request, User actor);
    CaseIntakeResponse review(Long caseId);
}
