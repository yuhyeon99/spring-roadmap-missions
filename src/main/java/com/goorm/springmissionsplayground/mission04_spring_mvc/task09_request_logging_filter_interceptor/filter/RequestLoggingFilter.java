package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String LOGGING_PATH_PREFIX = "/mission04/task09/logs";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(LOGGING_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestPath = buildRequestPath(request);

        request.setAttribute("task09FilterStartTime", startTime);

        log.info(
                "[Task09Filter][REQUEST] method={}, uri={}, startedAt={}",
                request.getMethod(),
                requestPath,
                LocalDateTime.now()
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            log.info(
                    "[Task09Filter][RESPONSE] method={}, uri={}, status={}, durationMs={}",
                    request.getMethod(),
                    requestPath,
                    response.getStatus(),
                    durationMs
            );
        }
    }

    private String buildRequestPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + queryString;
    }
}
