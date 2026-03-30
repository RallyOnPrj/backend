package com.gumraze.rallyon.backend.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final long DEFAULT_SLOW_REQUEST_THRESHOLD_MILLIS = 1_000L;

    private final long slowRequestThresholdMillis;

    public ApiLoggingFilter() {
        this(DEFAULT_SLOW_REQUEST_THRESHOLD_MILLIS);
    }

    ApiLoggingFilter(long slowRequestThresholdMillis) {
        this.slowRequestThresholdMillis = slowRequestThresholdMillis;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 로그 발생 시간 기록
        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 실행 시간 기록
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();

            if (!shouldLog(status, duration)) {
                return;
            }

            writeLog(request, status, duration);
        }
    }

    private boolean shouldLog(int status, long duration) {
        return status >= 400 || duration >= slowRequestThresholdMillis;
    }

    private void writeLog(HttpServletRequest request, int status, long duration) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if (status >= 500) {
            log.error("[API][SERVER_ERROR] method={}, path={}, status={}, duration={}",
                    method, path, status, duration);
            return;
        }

        if (status >= 400) {
            log.warn("[API][CLIENT_ERROR] method={}, path={}, status={}, duration={}",
                    method, path, status, duration);
            return;
        }

        log.warn("[API][SLOW] method={}, path={}, status={}, duration={}",
                method, path, status, duration);
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/swagger");
    }
}
