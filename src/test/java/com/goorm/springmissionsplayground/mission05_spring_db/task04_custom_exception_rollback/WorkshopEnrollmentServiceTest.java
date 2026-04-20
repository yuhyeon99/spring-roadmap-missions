package com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback;

import com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.exception.WorkshopCapacityExceededException;
import com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.repository.WorkshopEnrollmentRepository;
import com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.service.WorkshopEnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class WorkshopEnrollmentServiceTest {

    @Autowired
    private WorkshopEnrollmentService workshopEnrollmentService;

    @Autowired
    private WorkshopEnrollmentRepository workshopEnrollmentRepository;

    @BeforeEach
    void setUp() {
        workshopEnrollmentRepository.deleteAll();
    }

    @Test
    @DisplayName("정원 이하로 신청하면 트랜잭션이 커밋되고 신청 정보가 저장된다")
    void registerWithinCapacityCommitsTransaction() throws Exception {
        Long enrollmentId = workshopEnrollmentService.registerWithCapacityCheck(
                "TX-BASIC",
                "김커밋",
                "commit@example.com",
                1
        );

        assertThat(enrollmentId).isNotNull();
        assertThat(workshopEnrollmentService.countByWorkshopCode("TX-BASIC")).isEqualTo(1);
    }

    @Test
    @DisplayName("커스텀 체크 예외가 발생하면 rollbackFor 설정에 의해 트랜잭션이 롤백된다")
    void checkedExceptionTriggersRollback() {
        assertThatThrownBy(() -> workshopEnrollmentService.registerWithCapacityCheck(
                "TX-ROLLBACK",
                "김롤백",
                "rollback@example.com",
                0
        ))
                .isInstanceOf(WorkshopCapacityExceededException.class)
                .hasMessageContaining("워크숍 정원을 초과했습니다.");

        assertThat(workshopEnrollmentService.countByWorkshopCode("TX-ROLLBACK")).isZero();
    }

    @Test
    @DisplayName("두 번째 신청에서 예외가 나면 실패한 신청만 롤백되고 이전 커밋 데이터는 유지된다")
    void onlyFailingTransactionRollsBack() throws Exception {
        workshopEnrollmentService.registerWithCapacityCheck(
                "TX-KEEP-FIRST",
                "첫번째신청자",
                "first@example.com",
                1
        );

        assertThatThrownBy(() -> workshopEnrollmentService.registerWithCapacityCheck(
                "TX-KEEP-FIRST",
                "두번째신청자",
                "second@example.com",
                1
        ))
                .isInstanceOf(WorkshopCapacityExceededException.class);

        assertThat(workshopEnrollmentService.countByWorkshopCode("TX-KEEP-FIRST")).isEqualTo(1);
    }
}
