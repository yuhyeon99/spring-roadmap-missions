package com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.dto;

import jakarta.validation.constraints.NotBlank;

public class NotifyRequest {

    @NotBlank(message = "수신자는 필수입니다.")
    private String to;

    @NotBlank(message = "메시지는 필수입니다.")
    private String message;

    @NotBlank(message = "채널은 필수입니다.")
    private String channel;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
