package com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mission05_task04_workshop_enrollments")
public class WorkshopEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String workshopCode;

    @Column(nullable = false, length = 30)
    private String participantName;

    @Column(nullable = false, length = 100)
    private String participantEmail;

    protected WorkshopEnrollment() {
        // JPA 기본 생성자
    }

    public WorkshopEnrollment(String workshopCode, String participantName, String participantEmail) {
        this.workshopCode = workshopCode;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
    }

    public Long getId() {
        return id;
    }

    public String getWorkshopCode() {
        return workshopCode;
    }

    public String getParticipantName() {
        return participantName;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }
}
