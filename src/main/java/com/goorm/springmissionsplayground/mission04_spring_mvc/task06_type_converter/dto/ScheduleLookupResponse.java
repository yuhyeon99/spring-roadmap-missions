package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.dto;

import java.time.LocalDate;

public class ScheduleLookupResponse {

    private final LocalDate requestedDate;
    private final String dayOfWeek;
    private final String agenda;
    private final String message;

    public ScheduleLookupResponse(LocalDate requestedDate, String dayOfWeek, String agenda, String message) {
        this.requestedDate = requestedDate;
        this.dayOfWeek = dayOfWeek;
        this.agenda = agenda;
        this.message = message;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getAgenda() {
        return agenda;
    }

    public String getMessage() {
        return message;
    }
}
