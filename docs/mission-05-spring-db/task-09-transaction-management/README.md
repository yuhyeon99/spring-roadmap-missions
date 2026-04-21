# 트랜잭션 관리 이해 및 적용

이 문서는 `mission-05-spring-db`의 `task-09-transaction-management` 구현 결과를 정리한 보고서입니다. 두 계좌 사이의 송금 기능을 예제로 만들고, 송금 중간에 오류가 발생했을 때 `@Transactional`에 의해 출금과 입금이 함께 롤백되는지 테스트로 확인했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-05-spring-db` / `task-09-transaction-management`
- 목표:
  - 하나의 비즈니스 작업 안에서 여러 데이터베이스 변경이 함께 처리되도록 트랜잭션을 적용한다.
  - 송금 도중 오류가 발생하면 중간까지 반영된 변경도 모두 롤백되는지 검증한다.
  - 성공/실패 시 결과 차이를 코드와 문서로 정리한다.
- 예제 시나리오:
  - 보내는 계좌에서 금액을 차감한 뒤 받는 계좌에 같은 금액을 더하는 송금 기능을 구현합니다.
  - 출금 직후 예외를 일부러 발생시키는 실패 시나리오를 만들어, 입금 전에 오류가 나도 출금 결과까지 함께 되돌아가는지 확인합니다.
- 사용 기술: `Spring Boot`, `Spring Data JPA`, `H2 Database`, `@Transactional`, `JpaTransactionManager`, `@SpringBootTest`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Config | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/config/Task09JpaTransactionConfig.java` | `transactionManager` 이름의 기본 JPA 트랜잭션 매니저를 등록합니다. |
| Config | `src/main/resources/application.properties` | H2 메모리 DB와 JPA 공통 실행 환경을 제공합니다. |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/domain/TransferAccount.java` | 계좌번호, 예금주, 잔액과 출금/입금 로직을 가지는 송금용 엔터티입니다. |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/repository/TransferAccountRepository.java` | 계좌 저장과 계좌번호 기준 조회를 담당하는 JPA 리포지토리입니다. |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/service/TransferService.java` | 송금 트랜잭션을 적용하고, 성공/실패 흐름을 제어하는 핵심 서비스입니다. |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/exception/TransferSimulationException.java` | 송금 중간 실패 상황을 재현하기 위한 런타임 예외입니다. |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/TransferServiceTest.java` | 성공 커밋, 실패 롤백, 이전 성공 유지 시나리오를 통합 테스트로 검증합니다. |
| Artifact | `docs/mission-05-spring-db/task-09-transaction-management/task09-gradle-test-output.txt` | `task09` 테스트 실행 로그를 저장한 아티팩트입니다. |

## 3. 구현 단계와 주요 코드 해설

1. `TransferAccount` 엔터티를 만들고 `accountNumber`, `ownerName`, `balance`를 저장하도록 구성했습니다. 잔액 변경은 아무 곳에서나 하지 않고 `withdraw()`와 `deposit()` 메서드를 통해서만 수행하게 했습니다.
2. `TransferAccountRepository`는 `findByAccountNumber()` 메서드를 제공해 송금 대상 계좌를 계좌번호로 찾을 수 있게 했습니다.
3. `TransferService.transfer()`에 `@Transactional(transactionManager = "transactionManager")`를 선언했습니다. 이 메서드 안에서 출금과 입금이 하나의 트랜잭션으로 묶입니다.
4. 송금 순서는 `보내는 계좌 조회 -> 받는 계좌 조회 -> 출금 -> (선택적으로 예외 발생) -> 입금`입니다. `failAfterWithdraw`가 `true`면 출금이 끝난 직후 `TransferSimulationException`을 던져 중간 실패를 재현합니다.
5. 이 예외는 `RuntimeException`이므로 스프링 기본 규칙에 따라 현재 트랜잭션 전체가 롤백됩니다. 따라서 출금까지 이미 반영된 것처럼 보여도 최종 DB에는 남지 않습니다.
6. `Task09JpaTransactionConfig`는 `transactionManager` 이름의 기본 JPA 트랜잭션 매니저를 명시적으로 등록합니다. 기존 `task07`의 JDBC 전용 트랜잭션 매니저와 구분하면서, JPA 기반 송금 서비스가 어떤 매니저를 쓸지 분명하게 고정한 설정입니다.

### 성공/실패 결과 비교

| 시나리오 | 보내는 계좌 시작 잔액 | 받는 계좌 시작 잔액 | 처리 결과 | 최종 잔액 |
|---|---:|---:|---|---|
| 정상 송금 | 10,000 | 5,000 | 출금 3,000 후 입금 3,000이 모두 커밋됨 | 보내는 계좌 7,000 / 받는 계좌 8,000 |
| 중간 오류 발생 | 10,000 | 5,000 | 출금 후 예외가 발생해 전체 트랜잭션 롤백 | 보내는 계좌 10,000 / 받는 계좌 5,000 |

- 핵심 해석:
  - 트랜잭션이 없으면 출금만 반영되고 입금은 반영되지 않는 불완전한 상태가 생길 수 있습니다.
  - 이번 구현에서는 출금 후 오류가 나더라도 트랜잭션 전체가 롤백되어 두 계좌 잔액이 모두 원래 값으로 복구됩니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `Task09JpaTransactionConfig.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/config/Task09JpaTransactionConfig.java`
- 역할: 기본 JPA 트랜잭션 매니저 등록
- 상세 설명:
- `transactionManager`라는 이름의 `JpaTransactionManager`를 빈으로 등록합니다.
- `task09` 서비스는 이 이름을 명시적으로 사용해 JPA 엔터티 변경을 하나의 트랜잭션으로 묶습니다.
- JDBC 전용 트랜잭션 매니저가 이미 있는 프로젝트에서 JPA 쪽 기본 매니저를 분명히 지정하기 위한 설정입니다.

<details>
<summary><code>Task09JpaTransactionConfig.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class Task09JpaTransactionConfig {

    @Bean("transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
```

</details>

### 4.2 `application.properties`

- 파일 경로: `src/main/resources/application.properties`
- 역할: H2 메모리 DB와 JPA 실행 환경 제공
- 상세 설명:
- 인메모리 H2 데이터베이스를 사용해 외부 DB 없이 송금 테스트를 반복 실행할 수 있습니다.
- `spring.jpa.hibernate.ddl-auto=create-drop` 덕분에 `mission05_task09_transfer_accounts` 테이블이 테스트 실행 시 자동 생성됩니다.
- `spring.jpa.show-sql=true` 설정으로 필요하면 실제 insert/update SQL 흐름도 콘솔에서 추적할 수 있습니다.

<details>
<summary><code>application.properties</code> 전체 코드</summary>

```properties
spring.application.name=core

# Mission04 Task02: Thymeleaf View Resolver 설정
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false

# H2 in-memory DB 설정 (테스트/학습용)
spring.datasource.url=jdbc:h2:mem:mission01;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA 설정
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 콘솔 (개발 편의를 위해 활성화)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

</details>

### 4.3 `TransferAccount.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/domain/TransferAccount.java`
- 역할: 송금용 계좌 엔터티
- 상세 설명:
- `mission05_task09_transfer_accounts` 테이블에 매핑되며, 계좌번호는 `unique` 제약으로 중복을 막습니다.
- `withdraw()`는 금액이 0 이하이거나 잔액이 부족할 때 예외를 던집니다.
- `deposit()`는 0 이하 금액을 막고, 잔액 증가를 엔터티 내부에서 처리합니다.

<details>
<summary><code>TransferAccount.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mission05_task09_transfer_accounts")
public class TransferAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String accountNumber;

    @Column(nullable = false, length = 30)
    private String ownerName;

    @Column(nullable = false)
    private int balance;

    protected TransferAccount() {
        // JPA 기본 생성자
    }

    public TransferAccount(String accountNumber, String ownerName, int balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public void withdraw(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("출금 금액은 0보다 커야 합니다.");
        }
        if (balance < amount) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.balance -= amount;
    }

    public void deposit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다.");
        }
        this.balance += amount;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getBalance() {
        return balance;
    }
}
```

</details>

### 4.4 `TransferAccountRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/repository/TransferAccountRepository.java`
- 역할: 계좌 저장과 조회
- 상세 설명:
- `JpaRepository<TransferAccount, Long>`를 상속해 기본 CRUD 기능을 사용합니다.
- `findByAccountNumber()`로 계좌번호 기준 조회를 지원합니다.
- 서비스는 이 메서드를 통해 송금 대상 두 계좌를 불러옵니다.

<details>
<summary><code>TransferAccountRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.domain.TransferAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferAccountRepository extends JpaRepository<TransferAccount, Long> {

    Optional<TransferAccount> findByAccountNumber(String accountNumber);
}
```

</details>

### 4.5 `TransferService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/service/TransferService.java`
- 역할: 송금 트랜잭션 처리
- 상세 설명:
- 핵심 공개 메서드는 `createAccount()`, `transfer()`, `getBalance()`입니다.
- `transfer()`는 `@Transactional(transactionManager = "transactionManager")`로 선언되어 출금과 입금이 하나의 트랜잭션으로 처리됩니다.
- `failAfterWithdraw`가 `true`면 출금 직후 `TransferSimulationException`을 던져 전체 롤백을 확인할 수 있습니다.

<details>
<summary><code>TransferService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.domain.TransferAccount;
import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.exception.TransferSimulationException;
import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.repository.TransferAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final TransferAccountRepository transferAccountRepository;

    public TransferService(TransferAccountRepository transferAccountRepository) {
        this.transferAccountRepository = transferAccountRepository;
    }

    public Long createAccount(String accountNumber, String ownerName, int balance) {
        TransferAccount account = transferAccountRepository.save(
                new TransferAccount(accountNumber, ownerName, balance)
        );
        return account.getId();
    }

    @Transactional(transactionManager = "transactionManager")
    public void transfer(String fromAccountNumber, String toAccountNumber, int amount, boolean failAfterWithdraw) {
        TransferAccount fromAccount = getAccount(fromAccountNumber);
        TransferAccount toAccount = getAccount(toAccountNumber);

        fromAccount.withdraw(amount);

        if (failAfterWithdraw) {
            throw new TransferSimulationException("출금 후 입금 전에 오류가 발생해 전체 송금을 롤백합니다.");
        }

        toAccount.deposit(amount);
    }

    @Transactional(transactionManager = "transactionManager", readOnly = true)
    public int getBalance(String accountNumber) {
        return getAccount(accountNumber).getBalance();
    }

    private TransferAccount getAccount(String accountNumber) {
        return transferAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다. accountNumber=" + accountNumber));
    }
}
```

</details>

### 4.6 `TransferSimulationException.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/exception/TransferSimulationException.java`
- 역할: 송금 중간 실패 재현용 예외
- 상세 설명:
- `RuntimeException`을 상속한 예외입니다.
- 출금은 되었지만 입금 전에 오류가 난 상황을 코드로 분명하게 표현합니다.
- 런타임 예외이기 때문에 기본 스프링 트랜잭션 규칙으로도 전체 롤백 대상이 됩니다.

<details>
<summary><code>TransferSimulationException.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.exception;

public class TransferSimulationException extends RuntimeException {

    public TransferSimulationException(String message) {
        super(message);
    }
}
```

</details>

### 4.7 `TransferServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task09_transaction_management/TransferServiceTest.java`
- 역할: 송금 성공/실패 흐름 검증
- 상세 설명:
- `transferCommitsWhenNoErrorOccurs()`는 성공 시 출금과 입금이 함께 반영되는지 검증합니다.
- `transferRollsBackWhenErrorOccursInMiddle()`는 출금 후 예외가 발생했을 때 두 계좌 잔액이 모두 원래 값으로 되돌아가는지 확인합니다.
- `onlyFailingTransferTransactionRollsBack()`는 이전 성공 송금은 유지되고 이후 실패한 송금만 롤백되는지를 보장합니다.

<details>
<summary><code>TransferServiceTest.java</code> 전체 코드</summary>

```java
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
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 `@Transactional`

- 핵심:
  - 하나의 서비스 메서드 안에서 발생한 여러 DB 변경을 하나의 트랜잭션으로 묶습니다.
  - 메서드가 정상 종료되면 커밋하고, 예외가 발생하면 롤백합니다.
- 왜 쓰는가:
  - 송금처럼 “출금과 입금이 반드시 함께 성공해야 하는 작업”에서 데이터 정합성을 지키기 위해 필요합니다.
  - 이번 태스크에서는 출금 뒤 예외가 나더라도 출금 결과가 DB에 남지 않게 만드는 핵심 장치입니다.
- 참고 링크:
  - Spring `@Transactional` 레퍼런스: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html

### 5.2 `JpaTransactionManager`

- 핵심:
  - JPA의 `EntityManager` 기반 작업을 스프링 트랜잭션과 연결하는 트랜잭션 매니저입니다.
  - 같은 트랜잭션 안의 엔터티 변경을 추적하고 커밋 시점에 SQL 반영을 조정합니다.
- 왜 쓰는가:
  - 이번 태스크는 JPA 엔터티 잔액 변경을 사용하므로, JPA용 트랜잭션 매니저를 명확히 지정할 필요가 있었습니다.
  - 이미 JDBC 전용 트랜잭션 매니저가 있는 프로젝트라 기본 이름을 명시적으로 고정하는 것이 안전합니다.
- 참고 링크:
  - Spring `JpaTransactionManager` javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/orm/jpa/JpaTransactionManager.html

### 5.3 런타임 예외와 롤백

- 핵심:
  - 스프링 기본 규칙에서는 `RuntimeException`과 `Error`가 발생하면 현재 트랜잭션을 롤백합니다.
  - 체크 예외는 기본값만으로는 자동 롤백되지 않을 수 있습니다.
- 왜 쓰는가:
  - 이번 태스크의 `TransferSimulationException`은 실패 시 전체 롤백을 직관적으로 보여 주기 위해 런타임 예외로 만들었습니다.
  - `task04`의 체크 예외 기반 롤백과 비교해 보면 기본 규칙 차이도 함께 이해할 수 있습니다.
- 참고 링크:
  - Spring Transaction Rollback 레퍼런스: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html

## 6. 실행·검증 방법

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.TransferServiceTest
```

- 테스트 검증 포인트:
  - 정상 송금 후 잔액이 `10,000 -> 7,000`, `5,000 -> 8,000`으로 바뀌는지 확인합니다.
  - 실패 송금 후 잔액이 각각 `10,000`, `5,000`으로 그대로 유지되는지 확인합니다.
  - 이전에 성공한 송금 결과는 유지되고, 이후 실패한 송금만 롤백되는지 확인합니다.

```bash
./gradlew bootRun
```

- 앱 실행 후 H2 콘솔에서 직접 확인하려면:
  - `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:mission01`
  - 사용자명: `sa`
  - 비밀번호: 빈 값

## 7. 결과 확인 방법

- 성공 기준:
  - `TransferServiceTest`가 모두 통과해야 합니다.
  - 정상 송금은 두 계좌 잔액이 함께 변경되어야 합니다.
  - 실패 송금은 예외가 발생해도 두 계좌 잔액이 모두 원래 값으로 유지되어야 합니다.
- 결과 로그 파일:
  - 파일명: `task09-gradle-test-output.txt`
  - 저장 위치: `docs/mission-05-spring-db/task-09-transaction-management/task09-gradle-test-output.txt`

## 8. 학습 내용

- 트랜잭션은 “DB 작업을 묶는다”는 개념으로 끝나지 않고, 실패 시 어디까지 되돌릴지를 시스템 수준에서 보장해 줍니다.
- 송금 기능은 트랜잭션 필요성이 가장 분명한 예시입니다. 출금과 입금 중 하나만 반영되면 잔액이 맞지 않는 심각한 데이터 오류가 생기기 때문입니다.
- 이번 구현에서 중요한 점은 출금 메서드를 먼저 실행했더라도, 예외가 발생하면 그 결과가 최종 DB에 남지 않는다는 것입니다. 즉, 코드가 앞에서 실행됐다고 해서 무조건 커밋되는 것은 아닙니다.
- 또한 트랜잭션 매니저를 명시적으로 지정하는 습관이 도움이 됩니다. 프로젝트 안에 여러 데이터 접근 기술과 매니저가 함께 있을 때, 어떤 트랜잭션 경계를 사용하는지 코드를 읽는 사람도 바로 이해할 수 있습니다.
