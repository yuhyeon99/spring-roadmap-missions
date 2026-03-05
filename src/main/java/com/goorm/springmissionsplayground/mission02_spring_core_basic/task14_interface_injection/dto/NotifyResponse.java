package com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.dto;

public class NotifyResponse {

    private final String channel;
    private final String to;
    private final String message;

    public NotifyResponse(String channel, String to, String message) {
        this.channel = channel;
        this.to = to;
        this.message = message;
    }

    public String getChannel() {
        return channel;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }
}
