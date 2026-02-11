package com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.repository.MemberJpaRepository;
import com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.exception.TxSimulationException;
import com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.service.MemberTxService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class MemberTxServiceTest {

    @Autowired
    MemberTxService memberTxService;

    @Autowired
    MemberJpaRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    void success_commits_transaction() {
        memberTxService.create("tx-user", "tx@example.com");

        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    void runtime_exception_rolls_back() {
        assertThatThrownBy(() -> memberTxService.createWithFailure("tx-fail", "fail@example.com"))
                .isInstanceOf(TxSimulationException.class);

        assertThat(memberRepository.count()).isZero();
    }
}
