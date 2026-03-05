package com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.dto.NotifyRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.dto.NotifyResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.sender.NotificationSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("task14NotificationService")
public class NotificationService {

    private final Map<String, NotificationSender> senderMap;
    private final List<NotificationSender> senderList;

    public NotificationService(Map<String, NotificationSender> senderMap, List<NotificationSender> senderList) {
        this.senderMap = senderMap;
        this.senderList = senderList;
    }

    public NotifyResponse notify(NotifyRequest request) {
        NotificationSender sender = resolveSender(request.getChannel());
        sender.send(request.getTo(), request.getMessage());
        return new NotifyResponse(sender.channel(), request.getTo(), request.getMessage());
    }

    public Map<String, String> availableChannels() {
        return senderList.stream()
            .collect(Collectors.toMap(NotificationSender::channel, NotificationSender::channel));
    }

    private NotificationSender resolveSender(String channel) {
        return senderList.stream()
            .filter(s -> s.channel().equalsIgnoreCase(channel))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 채널: " + channel + " / 사용가능: " + senderMap.keySet()));
    }
}
