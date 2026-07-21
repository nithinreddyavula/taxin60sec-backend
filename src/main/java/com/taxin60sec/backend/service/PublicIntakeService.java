package com.taxin60sec.backend.service;

import com.taxin60sec.backend.dto.publicintake.PublicStartRequest;
import com.taxin60sec.backend.dto.publicintake.PublicStartResponse;
import com.taxin60sec.backend.dto.publicintake.NextAnswerRequest;
import com.taxin60sec.backend.dto.publicintake.PublicAnswerRequest;
import com.taxin60sec.backend.dto.publicintake.PublicAnswerResponse;
import com.taxin60sec.backend.dto.publicintake.PublicStartRequest;
import com.taxin60sec.backend.dto.publicintake.PublicStartResponse;
import com.taxin60sec.backend.dto.publicintake.ResumeIntakeResponse;

public interface PublicIntakeService {

    PublicStartResponse start(PublicStartRequest request);

    PublicAnswerResponse saveAnswer(
            Long caseId,
            PublicAnswerRequest request
    );

    PublicAnswerResponse next(
            Long caseId,
            NextAnswerRequest request
    );

    ResumeIntakeResponse resume(String token);
}