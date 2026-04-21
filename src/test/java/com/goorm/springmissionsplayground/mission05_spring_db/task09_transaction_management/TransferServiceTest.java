package com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management;

import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.exception.TransferSimulationException;
import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.repository.TransferAccountRepository;
import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferAccountRepository transferAccountRepository;

    @BeforeEach
    void setUp() {
        transferAccountRepository.deleteAll();
        transferService.createAccount("100-111", "보내는사람", 10_000);
        transferService.createAccount("200-222", "받는사람", 5_000);
    }

    @Test
    @DisplayName("송금이 정상 완료되면 출금과 입금이 함께 커밋된다")
    void transferCommitsWhenNoErrorOccurs() {
        transferService.transfer("100-111", "200-222", 3_000, false);

        assertThat(transferService.getBalance("100-111")).isEqualTo(7_000);
        assertThat(transferService.getBalance("200-222")).isEqualTo(8_000);
    }

    @Test
    @DisplayName("출금 후 예외가 발생하면 전체 트랜잭션이 롤백되어 두 계좌 잔액이 모두 원래 값으로 유지된다")
    void transferRollsBackWhenErrorOccursInMiddle() {
        assertThatThrownBy(() -> transferService.transfer("100-111", "200-222", 3_000, true))
                .isInstanceOf(TransferSimulationException.class)
                .hasMessageContaining("전체 송금을 롤백");

        assertThat(transferService.getBalance("100-111")).isEqualTo(10_000);
        assertThat(transferService.getBalance("200-222")).isEqualTo(5_000);
    }

    @Test
    @DisplayName("이전에 성공한 송금은 유지되고, 이후 실패한 송금만 롤백된다")
    void onlyFailingTransferTransactionRollsBack() {
        transferService.transfer("100-111", "200-222", 2_000, false);

        assertThatThrownBy(() -> transferService.transfer("100-111", "200-222", 1_000, true))
                .isInstanceOf(TransferSimulationException.class);

        assertThat(transferService.getBalance("100-111")).isEqualTo(8_000);
        assertThat(transferService.getBalance("200-222")).isEqualTo(7_000);
    }
}
