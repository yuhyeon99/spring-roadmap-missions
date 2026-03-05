package com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestTrace {

    private final String requestId = UUID.randomUUID().toString();
    private final LocalDateTime createdAt = LocalDateTime.now();

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
