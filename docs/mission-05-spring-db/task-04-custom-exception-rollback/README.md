# 트랜잭션 롤백을 위한 커스텀 예외 정의

이 문서는 `mission-05-spring-db`의 `task-04-custom-exception-rollback` 구현 결과를 정리한 보고서입니다. 워크숍 신청 정원을 초과하면 커스텀 체크 예외를 발생시키고, 해당 예외가 발생했을 때 트랜잭션이 실제로 롤백되는지 테스트 코드로 검증했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-05-spring-db` / `task-04-custom-exception-rollback`
- 목표:
  - 트랜잭션 롤백을 위한 커스텀 예외를 정의한다.
  - 특정 비즈니스 상황에서 해당 예외를 발생시키고, `@Transactional`의 롤백 규칙으로 트랜잭션이 되돌아가는지 확인한다.
  - 예외 코드, 롤백 서비스 코드, 테스트 결과를 문서와 함께 제출 가능한 형태로 정리한다.
- 특정 상황:
  - 같은 워크숍 코드(`workshopCode`)에 대해 허용된 최대 정원(`maxCapacity`)을 초과해 신청이 들어오면 예외를 발생시킵니다.
  - 예를 들어 최대 정원이 `1`인데 두 번째 신청이 들어오면, 두 번째 신청 데이터는 저장되지 않고 롤백되어야 합니다.
- 사용 기술: `Spring Boot`, `Spring Data JPA`, `Hibernate`, `H2 Database`, `@Transactional`, `Mock-free Integration Test`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Service | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/service/WorkshopEnrollmentService.java` | 신청 저장 후 정원 초과 여부를 검사하고, 커스텀 예외와 트랜잭션 롤백을 연결합니다. |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/repository/WorkshopEnrollmentRepository.java` | 신청 엔터티 저장과 워크숍별 신청 수 집계를 담당합니다. |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/domain/WorkshopEnrollment.java` | 워크숍 신청 데이터를 JPA 엔터티로 정의합니다. |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/exception/WorkshopCapacityExceededException.java` | 정원 초과 상황을 표현하는 커스텀 체크 예외입니다. |
| Config | `src/main/resources/application.properties` | H2, Hibernate DDL 자동 생성, SQL 출력 등 공통 실행 환경을 설정합니다. |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/WorkshopEnrollmentServiceTest.java` | 커밋 성공, 롤백 발생, 이전 커밋 유지 시나리오를 통합 테스트로 검증합니다. |
| Artifact | `docs/mission-05-spring-db/task-04-custom-exception-rollback/task04-gradle-test-output.txt` | `task04` 테스트 실행 결과를 저장한 콘솔 출력 파일입니다. |

## 3. 구현 단계와 주요 코드 해설

1. 이번 태스크의 특정 상황은 "워크숍 정원 초과"입니다. 서비스는 신청 정보를 먼저 저장한 뒤 현재 워크숍 신청 수를 조회하고, 그 값이 최대 정원을 넘으면 `WorkshopCapacityExceededException`을 던지도록 구성했습니다.
2. `WorkshopCapacityExceededException`은 `Exception`을 상속하는 체크 예외로 만들었습니다. 이렇게 하면 스프링의 기본 규칙만으로는 자동 롤백되지 않기 때문에, `rollbackFor` 설정이 왜 필요한지 더 분명하게 확인할 수 있습니다.
3. `WorkshopEnrollmentService.registerWithCapacityCheck()`에는 `@Transactional(rollbackFor = WorkshopCapacityExceededException.class)`를 선언했습니다. 덕분에 체크 예외가 발생해도 해당 트랜잭션은 롤백 대상으로 처리됩니다.
4. 저장 직후 `countByWorkshopCode()`를 호출해 현재 신청 수를 다시 계산하도록 했습니다. 이 시점에 정원 초과가 감지되면 이미 `save()`가 호출되었더라도 예외 발생과 함께 전체 트랜잭션이 되돌아갑니다.
5. 테스트는 세 가지 흐름으로 나눴습니다. 첫 번째는 정상 커밋, 두 번째는 첫 신청부터 정원 초과로 인한 전체 롤백, 세 번째는 두 번째 신청만 롤백되고 첫 번째 신청 데이터는 유지되는지 검증하는 흐름입니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `WorkshopEnrollmentService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/service/WorkshopEnrollmentService.java`
- 역할: 정원 검증과 트랜잭션 롤백 처리
- 상세 설명:
- 핵심 공개 메서드는 `registerWithCapacityCheck()`와 `countByWorkshopCode()`입니다.
- `registerWithCapacityCheck()`는 `@Transactional(rollbackFor = WorkshopCapacityExceededException.class)`로 선언되어, 체크 예외가 발생해도 롤백되도록 설정했습니다.
- 저장 후 바로 현재 신청 수를 조회하고, 정원을 초과하면 커스텀 예외를 던집니다.

<details>
<summary><code>WorkshopEnrollmentService.java</code> 전체 코드</summary>

```java
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
```

</details>

### 4.2 `WorkshopEnrollmentRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/repository/WorkshopEnrollmentRepository.java`
- 역할: 신청 저장과 워크숍별 신청 수 조회
- 상세 설명:
- `JpaRepository<WorkshopEnrollment, Long>`를 상속해 기본 CRUD 기능을 제공합니다.
- `countByWorkshopCode()`는 메서드 이름 기반 쿼리로 현재 워크숍 신청 수를 계산합니다.
- 서비스 계층은 이 메서드 결과를 기준으로 정원 초과 여부를 판단합니다.

<details>
<summary><code>WorkshopEnrollmentRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.domain.WorkshopEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkshopEnrollmentRepository extends JpaRepository<WorkshopEnrollment, Long> {

    long countByWorkshopCode(String workshopCode);
}
```

</details>

### 4.3 `WorkshopEnrollment.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/domain/WorkshopEnrollment.java`
- 역할: 워크숍 신청 엔터티
- 상세 설명:
- `@Entity`, `@Table(name = "mission05_task04_workshop_enrollments")`로 `task04` 전용 테이블에 매핑했습니다.
- 워크숍 코드, 신청자 이름, 신청자 이메일을 저장합니다.
- 롤백 검증에서는 이 엔터티의 insert가 예외 발생 후 실제로 사라지는지가 핵심입니다.

<details>
<summary><code>WorkshopEnrollment.java</code> 전체 코드</summary>

```java
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
```

</details>

### 4.4 `WorkshopCapacityExceededException.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/exception/WorkshopCapacityExceededException.java`
- 역할: 정원 초과 상황을 표현하는 커스텀 체크 예외
- 상세 설명:
- `Exception`을 상속한 체크 예외라서, 기본 스프링 트랜잭션 규칙만으로는 자동 롤백되지 않습니다.
- 예외 메시지 안에 `workshopCode`, 현재 신청 수, 최대 정원을 함께 담아 디버깅과 문서화에 활용할 수 있게 했습니다.
- 이번 태스크의 "예외 코드" 제출물에 해당하는 핵심 파일입니다.

<details>
<summary><code>WorkshopCapacityExceededException.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.exception;

public class WorkshopCapacityExceededException extends Exception {

    private final String workshopCode;
    private final long requestedCount;
    private final long maxCapacity;

    public WorkshopCapacityExceededException(String workshopCode, long requestedCount, long maxCapacity) {
        super("워크숍 정원을 초과했습니다. workshopCode=%s, 현재 신청 수=%d, 최대 정원=%d"
                .formatted(workshopCode, requestedCount, maxCapacity));
        this.workshopCode = workshopCode;
        this.requestedCount = requestedCount;
        this.maxCapacity = maxCapacity;
    }

    public String getWorkshopCode() {
        return workshopCode;
    }

    public long getRequestedCount() {
        return requestedCount;
    }

    public long getMaxCapacity() {
        return maxCapacity;
    }
}
```

</details>

### 4.5 `application.properties`

- 파일 경로: `src/main/resources/application.properties`
- 역할: H2와 JPA 공통 실행 설정
- 상세 설명:
- 인메모리 H2 데이터베이스와 Hibernate DDL 자동 생성 설정을 통해 롤백 테스트를 빠르게 반복할 수 있게 합니다.
- `ddl-auto=create-drop` 덕분에 테스트 실행 시 `mission05_task04_workshop_enrollments` 테이블이 자동 생성되고 종료 시 정리됩니다.
- `show-sql=true` 설정으로 필요하면 실제 SQL 실행 흔적도 콘솔에서 추적할 수 있습니다.

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

### 4.6 `WorkshopEnrollmentServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task04_custom_exception_rollback/WorkshopEnrollmentServiceTest.java`
- 역할: 롤백 동작 통합 검증
- 상세 설명:
- 검증 시나리오는 정상 커밋, 첫 신청부터 롤백, 두 번째 신청만 롤백의 세 가지입니다.
- 서비스 메서드를 직접 호출해 체크 예외 발생 시 트랜잭션 경계와 DB 상태를 함께 검증합니다.
- 정상/예외 흐름을 모두 포함해 커스텀 예외가 실제 롤백으로 이어지는지 보장합니다.

<details>
<summary><code>WorkshopEnrollmentServiceTest.java</code> 전체 코드</summary>

```java
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
```

</details>

### 4.7 `task04-gradle-test-output.txt`

- 파일 경로: `docs/mission-05-spring-db/task-04-custom-exception-rollback/task04-gradle-test-output.txt`
- 역할: `task04` 테스트 실행 결과 보관
- 상세 설명:
- `WorkshopEnrollmentServiceTest` 실행 결과를 저장한 콘솔 출력 파일입니다.
- 테스트가 이미 최신 상태일 때도 `BUILD SUCCESSFUL`로 결과를 다시 확인할 수 있습니다.
- 제출 시 롤백 테스트 결과를 빠르게 확인하는 보조 자료로 사용할 수 있습니다.

<details>
<summary><code>task04-gradle-test-output.txt</code> 내용</summary>

```text
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava UP-TO-DATE
> Task :processTestResources NO-SOURCE
> Task :testClasses UP-TO-DATE
> Task :test UP-TO-DATE

BUILD SUCCESSFUL in 473ms
4 actionable tasks: 4 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.0/userguide/configuration_cache_enabling.html
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 체크 예외와 트랜잭션 롤백 규칙

- 핵심:
- 스프링 트랜잭션의 기본 규칙은 `RuntimeException` 같은 언체크 예외에서만 자동 롤백하는 것입니다.
- 체크 예외를 던질 때는 `@Transactional(rollbackFor = 예외클래스.class)` 같은 롤백 규칙을 직접 선언해야 합니다.
- 왜 쓰는가:
- 비즈니스 예외를 체크 예외로 표현하면서도, 그 예외가 발생했을 때 데이터 변경을 반드시 되돌리고 싶은 경우가 있습니다.
- 이번 태스크처럼 "정원 초과" 같은 업무 규칙 위반을 명시적인 예외 타입으로 표현할 수 있습니다.
- 참고 링크:
- Spring Framework Reference - Rolling Back a Declarative Transaction: <https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html>

### 5.2 서비스 계층의 트랜잭션 경계

- 핵심:
- 여러 저장/조회 작업과 비즈니스 규칙 검사를 하나의 작업 단위로 묶으려면 서비스 계층에서 트랜잭션 경계를 잡는 것이 일반적입니다.
- Repository 자체의 기본 트랜잭션보다 바깥쪽 서비스 메서드의 `@Transactional` 설정이 실제 동작을 결정합니다.
- 왜 쓰는가:
- 저장 직후 검증, 예외 발생, 롤백까지를 하나의 일관된 흐름으로 다루려면 서비스 메서드 단위 트랜잭션이 필요합니다.
- 비즈니스 규칙이 복잡해질수록 서비스 계층에서 트랜잭션 의미를 명확히 두는 편이 유지보수에 유리합니다.
- 참고 링크:
- Spring Data JPA Reference - Transactionality: <https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html>
- Spring Framework Reference - Using `@Transactional`: <https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html>

### 5.3 JPA 엔터티와 롤백 검증

- 핵심:
- JPA 엔터티는 테이블 한 행을 자바 객체로 다루기 위한 단위이며, 트랜잭션이 커밋되면 실제 DB에 반영되고 롤백되면 변경이 취소됩니다.
- 이번 태스크에서는 `WorkshopEnrollment` insert가 예외 후 실제로 남지 않는지를 통해 롤백을 확인했습니다.
- 왜 쓰는가:
- 트랜잭션 설명을 로그나 이론으로만 보는 것보다, 엔터티 저장 결과가 남는지 사라지는지로 확인하면 동작을 훨씬 명확하게 이해할 수 있습니다.
- DB 작업의 원자성 개념을 JPA 코드 수준에서 직접 체감할 수 있습니다.
- 참고 링크:
- Jakarta Persistence `@Entity`: <https://jakarta.ee/specifications/persistence/3.1/apidocs/jakarta.persistence/jakarta/persistence/entity>

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

- 예상 결과:
- 애플리케이션이 `8080` 포트에서 실행됩니다.
- 콘솔에 Hibernate가 `mission05_task04_workshop_enrollments` 테이블을 생성하는 로그를 출력합니다.

### 6.2 API 호출 또는 화면 접근 방법

```text
이 태스크는 별도 REST API나 화면 대신 서비스 계층 테스트 코드로 동작을 검증합니다.
```

- 확인 대상:
- `WorkshopEnrollmentServiceTest`가 커스텀 예외 발생과 롤백 결과를 직접 검증합니다.
- 특히 `checkedExceptionTriggersRollback()` 테스트가 "체크 예외 + rollbackFor" 조합의 핵심 검증 시나리오입니다.

### 6.3 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission05_spring_db.task04_custom_exception_rollback.WorkshopEnrollmentServiceTest
```

- 예상 결과:
- 3개의 테스트가 모두 통과합니다.
- 콘솔 마지막에 `BUILD SUCCESSFUL`이 출력됩니다.
- HTML 테스트 리포트는 `build/reports/tests/test/index.html`에서 확인할 수 있습니다.

## 7. 결과 확인 방법

- 성공 기준:
- `registerWithinCapacityCommitsTransaction()`에서 워크숍 신청 수가 `1`로 남으면 정상 커밋입니다.
- `checkedExceptionTriggersRollback()`에서 `WorkshopCapacityExceededException`이 발생하고, 같은 워크숍 코드의 신청 수가 `0`이면 롤백 성공입니다.
- `onlyFailingTransactionRollsBack()`에서 첫 번째 신청은 유지되고 두 번째 신청만 롤백되어 최종 신청 수가 `1`이면 기대한 트랜잭션 경계가 유지된 것입니다.
- 결과 확인 파일:
- 콘솔 실행 결과 파일명: `task04-gradle-test-output.txt`
- 저장 위치: `./task04-gradle-test-output.txt`
- 추가 확인 경로:
- HTML 테스트 리포트: `build/reports/tests/test/index.html`

## 8. 학습 내용

이번 태스크에서 가장 중요한 지점은 "예외를 던졌다"와 "트랜잭션이 롤백되었다"가 같은 말이 아니라는 점이었습니다. 스프링은 기본적으로 언체크 예외에서만 자동 롤백하기 때문에, 체크 예외를 사용하면 예외가 발생해도 커밋될 수 있습니다. 그래서 `WorkshopCapacityExceededException`을 체크 예외로 만들고 `rollbackFor`를 명시해, 어떤 예외가 데이터 변경을 되돌려야 하는지를 코드에 분명히 드러냈습니다.

또 하나 확인한 점은 롤백은 "저장 전에 막는 것"이 아니라 "저장까지 진행되었더라도 작업 전체를 무효화하는 것"이라는 사실입니다. 이번 예제는 `save()`를 먼저 수행한 뒤 정원 초과를 감지하고 예외를 던지는데, 그럼에도 최종 DB 상태에는 해당 데이터가 남지 않습니다. 이 흐름을 테스트로 직접 확인해 보니, 트랜잭션의 원자성이 서비스 계층에서 왜 중요한지 더 명확하게 이해할 수 있었습니다.
