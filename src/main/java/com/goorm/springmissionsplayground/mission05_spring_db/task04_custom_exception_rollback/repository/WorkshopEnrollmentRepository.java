package com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.domain.WorkshopEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkshopEnrollmentRepository extends JpaRepository<WorkshopEnrollment, Long> {

    long countByWorkshopCode(String workshopCode);
}
