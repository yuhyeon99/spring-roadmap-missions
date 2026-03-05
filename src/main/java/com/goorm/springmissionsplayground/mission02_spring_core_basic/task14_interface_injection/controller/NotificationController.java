package com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.dto.NotifyRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.dto.NotifyResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task14_interface_injection.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("task14NotificationController")
@RequestMapping("/mission02/task14/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotifyResponse send(@RequestBody @Valid NotifyRequest request) {
        return notificationService.notify(request);
    }

    @GetMapping("/channels")
    public Map<String, String> channels() {
        return notificationService.availableChannels();
    }
}
