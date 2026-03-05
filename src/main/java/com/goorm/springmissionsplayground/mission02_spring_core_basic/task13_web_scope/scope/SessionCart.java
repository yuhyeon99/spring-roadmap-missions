package com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionCart {

    private final String sessionId = "session-" + UUID.randomUUID();
    private final AtomicInteger itemCount = new AtomicInteger(0);

    public int addItem() {
        return itemCount.incrementAndGet();
    }

    public int getItemCount() {
        return itemCount.get();
    }

    public String getSessionId() {
        return sessionId;
    }
}
