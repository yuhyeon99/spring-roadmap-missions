package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.service;

import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.sender.NotificationSender;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final List<NotificationSender> senders;
    private final MessageFormatter formatter;

    @Autowired
    public NotificationService(List<NotificationSender> senders, MessageFormatter formatter) {
        this.senders = senders;
        this.formatter = formatter;
    }

    public List<String> notifyAllChannels(String message) {
        String formatted = formatter.format(message);
        return senders.stream()
                .map(sender -> sender.send(formatted))
                .collect(Collectors.toList());
    }
}
