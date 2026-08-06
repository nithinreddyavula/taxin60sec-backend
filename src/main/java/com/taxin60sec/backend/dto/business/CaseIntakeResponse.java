package com.taxin60sec.backend.dto.business;

import com.taxin60sec.backend.dto.domain.CaseDto;
import com.taxin60sec.backend.dto.domain.RequiredDocumentDto;
import com.taxin60sec.backend.dto.domain.TimelineEventDto;
import com.taxin60sec.backend.dto.domain.UploadedDocumentDto;

import java.util.List;
import java.util.Map;

public record CaseIntakeResponse(
        CaseDto taxCase,
        String customerName,
        String customerEmail,
        String customerPhone,
        String serviceName,
        List<String> questions,
        Map<String, String> answers,
        List<UploadedDocumentDto> documents,
        List<RequiredDocumentDto> missingDocuments,
        List<TimelineEventDto> timeline
) { }
