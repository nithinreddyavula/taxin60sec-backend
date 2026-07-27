package com.taxin60sec.backend.service;

import com.taxin60sec.backend.dto.healthscore.TaxHealthCheckRequest;
import com.taxin60sec.backend.dto.healthscore.TaxHealthCheckResponse;

public interface TaxHealthCheckService {

    TaxHealthCheckResponse submit(TaxHealthCheckRequest request);

    TaxHealthCheckResponse getByShareToken(String shareToken);
}
