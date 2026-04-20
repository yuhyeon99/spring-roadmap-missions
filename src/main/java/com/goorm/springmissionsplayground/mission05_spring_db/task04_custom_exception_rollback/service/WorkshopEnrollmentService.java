package com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.domain.WorkshopEnrollment;
import com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.exception.WorkshopCapacityExceededException;
import com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.repository.WorkshopEnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkshopEnrollmentService {

    private final WorkshopEnrollmentRepository workshopEnrollmentRepository;

    public WorkshopEnrollmentService(WorkshopEnrollmentRepository workshopEnrollmentRepository) {
        this.workshopEnrollmentRepository = workshopEnrollmentRepository;
    }

    @Transactional(rollbackFor = WorkshopCapacityExceededException.class)
    public Long registerWithCapacityCheck(
            String workshopCode,
            String participantName,
            String participantEmail,
            long maxCapacity
    ) throws WorkshopCapacityExceededException {
        WorkshopEnrollment enrollment = workshopEnrollmentRepository.save(
                new WorkshopEnrollment(workshopCode, participantName, participantEmail)
        );

        long currentCount = workshopEnrollmentRepository.countByWorkshopCode(workshopCode);
        if (currentCount > maxCapacity) {
            throw new WorkshopCapacityExceededException(workshopCode, currentCount, maxCapacity);
        }

        return enrollment.getId();
    }

    @Transactional(readOnly = true)
    public long countByWorkshopCode(String workshopCode) {
        return workshopEnrollmentRepository.countByWorkshopCode(workshopCode);
    }
}
