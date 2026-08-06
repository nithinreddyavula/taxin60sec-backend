package com.taxin60sec.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Lightweight per-IP sliding-window rate limiter. Not a replacement for a proper
 * distributed limiter (e.g. Redis-backed) if this ever runs on multiple instances,
 * but closes the "zero rate limiting on public endpoints" gap for a single-instance
 * deployment at current scale.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_MILLIS = 60_000;

    private final ConcurrentHashMap<String, Deque<Long>> requestsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!isRateLimited(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = requestsByIp.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        synchronized (timestamps) {

            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Too many requests, please slow down\",\"errors\":[{\"code\":\"RATE_LIMITED\"}]}"
                );
                return;
            }

            timestamps.addLast(now);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String path) {
        return path.startsWith("/api/v1/public/")
                || path.startsWith("/api/v1/payments/");
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}