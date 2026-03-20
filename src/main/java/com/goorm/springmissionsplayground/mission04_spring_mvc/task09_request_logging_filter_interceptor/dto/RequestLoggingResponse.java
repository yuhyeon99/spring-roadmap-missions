package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.dto;

public class RequestLoggingResponse {

    private final String method;
    private final String requestUri;
    private final String topic;
    private final String processedAt;
    private final String message;

    public RequestLoggingResponse(String method, String requestUri, String topic, String processedAt, String message) {
        this.method = method;
        this.requestUri = requestUri;
        this.topic = topic;
        this.processedAt = processedAt;
        this.message = message;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getTopic() {
        return topic;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public String getMessage() {
        return message;
    }
}
