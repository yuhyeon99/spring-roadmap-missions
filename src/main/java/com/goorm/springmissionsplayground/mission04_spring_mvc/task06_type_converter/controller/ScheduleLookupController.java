package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.dto.ScheduleLookupResponse;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.service.ScheduleLookupService;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission04/task06/schedules")
public class ScheduleLookupController {

    private final ScheduleLookupService scheduleLookupService;

    public ScheduleLookupController(ScheduleLookupService scheduleLookupService) {
        this.scheduleLookupService = scheduleLookupService;
    }

    @GetMapping
    public ScheduleLookupResponse findByDate(@RequestParam("date") LocalDate date) {
        return scheduleLookupService.findSchedule(date);
    }
}
