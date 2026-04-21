package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level;

import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.dto.IsolationLevelObservation;
import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.repository.IsolationInventoryRepository;
import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.service.IsolationLevelStudyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IsolationLevelStudyServiceTest {

    @Autowired
    private IsolationLevelStudyService isolationLevelStudyService;

    @Autowired
    private IsolationInventoryRepository isolationInventoryRepository;

    @BeforeEach
    void setUp() {
        isolationInventoryRepository.deleteAll();
    }

    @Test
    @DisplayName("READ_COMMITTED에서는 같은 트랜잭션 안에서 두 번째 조회 시 커밋된 변경 값을 다시 읽을 수 있다")
    void readCommittedAllowsNonRepeatableRead() {
        Long itemId = isolationLevelStudyService.createSampleItem("스프링 트랜잭션 핸드북", 10);

        IsolationLevelObservation observation = isolationLevelStudyService.observeReadCommitted(itemId, 25);

        assertThat(observation.isolationLevel()).isEqualTo("READ_COMMITTED");
        assertThat(observation.firstQuantity()).isEqualTo(10);
        assertThat(observation.secondQuantity()).isEqualTo(25);
        assertThat(observation.observedSameValue()).isFalse();
        assertThat(isolationLevelStudyService.findItem(itemId).quantity()).isEqualTo(25);
    }

    @Test
    @DisplayName("REPEATABLE_READ에서는 같은 트랜잭션 안에서 두 번째 조회를 해도 처음 읽은 값을 유지한다")
    void repeatableReadKeepsFirstSnapshot() {
        Long itemId = isolationLevelStudyService.createSampleItem("스프링 트랜잭션 핸드북", 10);

        IsolationLevelObservation observation = isolationLevelStudyService.observeRepeatableRead(itemId, 25);

        assertThat(observation.isolationLevel()).isEqualTo("REPEATABLE_READ");
        assertThat(observation.firstQuantity()).isEqualTo(10);
        assertThat(observation.secondQuantity()).isEqualTo(10);
        assertThat(observation.observedSameValue()).isTrue();
        assertThat(isolationLevelStudyService.findItem(itemId).quantity()).isEqualTo(25);
    }
}
