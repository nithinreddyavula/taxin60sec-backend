package com.taxin60sec.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.ErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        ErrorDetail error = new ErrorDetail(ApiErrorCode.UNAUTHORIZED.name(), null, "Authentication is required");
        ApiResponse<Void> body = ApiResponse.error("Unauthorized", List.of(error), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
