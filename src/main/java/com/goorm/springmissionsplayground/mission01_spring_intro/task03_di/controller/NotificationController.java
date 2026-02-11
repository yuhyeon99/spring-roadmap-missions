package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.controller;

import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.dto.NotificationRequest;
import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.dto.NotificationResponse;
import com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.service.NotificationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission01/task03/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse notifyAll(@RequestBody NotificationRequest request) {
        List<String> results = notificationService.notifyAllChannels(request.getMessage());
        return new NotificationResponse(results);
    }
}
