package com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.dto;

import java.time.LocalDateTime;

public class ScopeSnapshot {

    private final RequestScopeInfo request;
    private final SessionScopeInfo session;
    private final ApplicationScopeInfo application;

    public ScopeSnapshot(RequestScopeInfo request, SessionScopeInfo session, ApplicationScopeInfo application) {
        this.request = request;
        this.session = session;
        this.application = application;
    }

    public RequestScopeInfo getRequest() {
        return request;
    }

    public SessionScopeInfo getSession() {
        return session;
    }

    public ApplicationScopeInfo getApplication() {
        return application;
    }

    public record RequestScopeInfo(String requestId, LocalDateTime createdAt) { }

    public record SessionScopeInfo(String sessionId, int itemCount) { }

    public record ApplicationScopeInfo(long totalHits) { }
}
