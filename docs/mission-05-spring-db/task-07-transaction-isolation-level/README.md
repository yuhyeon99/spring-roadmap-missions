# 트랜잭션 Isolation Level 설정 테스트

이 문서는 `mission-05-spring-db`의 `task-07-transaction-isolation-level` 구현 결과를 정리한 보고서입니다. `@Transactional(isolation = ...)` 설정으로 `READ_COMMITTED`와 `REPEATABLE_READ`를 각각 적용하고, 같은 트랜잭션 안에서 두 번 조회할 때 중간에 커밋된 변경을 어떻게 읽는지 테스트로 비교했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-05-spring-db` / `task-07-transaction-isolation-level`
- 목표:
  - 스프링 트랜잭션 격리 수준을 코드로 설정한다.
  - `READ_COMMITTED`와 `REPEATABLE_READ`가 데이터 일관성에 어떤 차이를 만드는지 테스트로 확인한다.
  - 테스트 결과와 해석을 문서로 정리해 제출 가능한 형태로 남긴다.
- 실험 시나리오:
  - 재고 수량이 `10`인 상품을 하나 준비합니다.
  - 바깥 트랜잭션이 재고를 한 번 읽은 뒤, 안쪽의 독립 트랜잭션(`REQUIRES_NEW`)이 수량을 `25`로 수정하고 커밋합니다.
  - 바깥 트랜잭션이 같은 행을 다시 읽었을 때 값이 바뀌는지 비교합니다.
- 사용 기술: `Spring Boot`, `Spring JDBC`, `JdbcTemplate`, `H2 Database`, `DataSourceTransactionManager`, `@Transactional`, `@SpringBootTest`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Config | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/config/Task07JdbcTransactionConfig.java` | `task07` 전용 JDBC 트랜잭션 매니저를 등록해 격리 수준 설정을 명확히 적용합니다. |
| Config | `src/main/resources/application.properties` | 공통 H2 메모리 DB와 JPA/JDBC 실행 환경을 제공합니다. |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/domain/IsolationInventoryItem.java` | 재고 ID, 상품명, 수량을 담는 조회용 도메인 레코드입니다. |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/dto/IsolationLevelObservation.java` | 첫 번째 조회값과 두 번째 조회값을 비교해 실험 결과를 표현합니다. |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/repository/IsolationInventoryRepository.java` | `JdbcTemplate`로 테이블 생성, 저장, 조회, 수정, 테스트 데이터 정리를 처리합니다. |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/service/IsolationLevelStudyService.java` | `READ_COMMITTED`, `REPEATABLE_READ` 실험 흐름을 각각 트랜잭션 경계로 분리합니다. |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/service/IsolationLevelWriteService.java` | `REQUIRES_NEW`로 독립 커밋을 발생시켜 바깥 트랜잭션과 비교 가능하게 만듭니다. |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/IsolationLevelStudyServiceTest.java` | 두 격리 수준의 조회 결과 차이와 최종 커밋 상태를 통합 테스트로 검증합니다. |
| Artifact | `docs/mission-05-spring-db/task-07-transaction-isolation-level/task07-gradle-test-output.txt` | `task07` 테스트 실행 로그를 저장한 아티팩트입니다. |

## 3. 구현 단계와 주요 코드 해설

1. 이번 태스크는 JPA 대신 `JdbcTemplate`을 사용했습니다. 같은 트랜잭션 안에서 JPA 엔터티를 두 번 조회하면 1차 캐시가 먼저 작동해 실제 DB 격리 수준 차이가 가려질 수 있기 때문입니다.
2. `Task07JdbcTransactionConfig`에서 `task07TransactionManager`를 `DataSourceTransactionManager`로 등록했습니다. 이렇게 하면 `JdbcTemplate`이 사용하는 JDBC 커넥션에 격리 수준이 직접 적용되어, 실험 의도가 더 분명해집니다.
3. `IsolationLevelStudyService`는 같은 구조의 실험 메서드 두 개를 제공합니다. `observeReadCommitted()`는 `Isolation.READ_COMMITTED`, `observeRepeatableRead()`는 `Isolation.REPEATABLE_READ`로 선언했습니다.
4. 두 메서드는 모두 같은 흐름을 탑니다. 먼저 현재 수량을 한 번 읽고, 그 다음 `IsolationLevelWriteService.updateQuantityInNewTransaction()`을 호출해 안쪽 트랜잭션에서 수량을 `25`로 바꾸고 커밋한 뒤, 바깥 트랜잭션이 같은 행을 다시 읽습니다.
5. 안쪽 서비스는 `Propagation.REQUIRES_NEW`를 사용합니다. 그래서 바깥 트랜잭션을 그대로 공유하지 않고, 별도의 물리 트랜잭션으로 update를 수행하고 즉시 커밋합니다.
6. 테스트는 바깥 트랜잭션 안에서 관찰한 값과, 트랜잭션이 모두 끝난 뒤 실제 DB에 남은 최종 값을 함께 검증합니다. 덕분에 “트랜잭션 내부에서 무엇을 봤는지”와 “최종 커밋 결과가 무엇인지”를 분리해서 이해할 수 있습니다.

### 테스트 결과 해석

| 격리 수준 | 첫 번째 조회 | 안쪽 트랜잭션 커밋 후 두 번째 조회 | 해석 |
|---|---:|---:|---|
| `READ_COMMITTED` | 10 | 25 | 이미 커밋된 변경을 두 번째 조회에서 다시 읽었습니다. 같은 트랜잭션 안에서도 비반복 읽기(non-repeatable read)가 발생할 수 있음을 보여줍니다. |
| `REPEATABLE_READ` | 10 | 10 | 두 번째 조회에서도 처음 읽은 값을 유지했습니다. 같은 행을 반복 조회할 때 더 안정적인 읽기 일관성을 제공합니다. |

- 공통 확인 사항:
  - 바깥 트랜잭션이 `REPEATABLE_READ`로 10을 계속 읽더라도, 안쪽 `REQUIRES_NEW` 트랜잭션이 커밋한 최종 DB 값은 25로 남습니다.
  - 즉, `REPEATABLE_READ`는 “다른 트랜잭션의 커밋이 없었다”는 뜻이 아니라, “현재 트랜잭션이 같은 행을 다시 읽을 때 처음 본 값을 유지한다”는 뜻에 가깝습니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `Task07JdbcTransactionConfig.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/config/Task07JdbcTransactionConfig.java`
- 역할: `task07` 전용 JDBC 트랜잭션 매니저 등록
- 상세 설명:
- `task07TransactionManager`라는 이름의 `DataSourceTransactionManager`를 빈으로 등록합니다.
- `IsolationLevelStudyService`, `IsolationLevelWriteService`는 이 매니저를 명시적으로 사용해 JDBC 커넥션 단위의 격리 수준과 전파 속성을 적용합니다.
- JPA 트랜잭션 설정과 섞이지 않게 범위를 분명히 나눈 것이 핵심입니다.

<details>
<summary><code>Task07JdbcTransactionConfig.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class Task07JdbcTransactionConfig {

    @Bean("task07TransactionManager")
    public PlatformTransactionManager task07TransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
```

</details>

### 4.2 `application.properties`

- 파일 경로: `src/main/resources/application.properties`
- 역할: 공통 H2 메모리 DB 실행 환경 제공
- 상세 설명:
- `spring.datasource.url=jdbc:h2:mem:mission01` 설정으로 인메모리 H2 데이터베이스를 사용합니다.
- `task07`의 JDBC 저장소도 같은 `DataSource`를 사용하므로 별도 DB 준비 없이 바로 테스트할 수 있습니다.
- `spring.h2.console.enabled=true`가 켜져 있어 애플리케이션 실행 중 `/h2-console`에서 DB 상태를 확인할 수 있습니다.

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

### 4.3 `IsolationInventoryItem.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/domain/IsolationInventoryItem.java`
- 역할: 재고 조회 결과 표현
- 상세 설명:
- `id`, `productName`, `quantity` 세 값을 가지는 간단한 레코드입니다.
- 저장소가 `JdbcTemplate` 조회 결과를 이 객체로 매핑합니다.
- 실험이 끝난 뒤 실제 DB 최종 수량이 얼마인지 확인할 때 사용합니다.

<details>
<summary><code>IsolationInventoryItem.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.domain;

public record IsolationInventoryItem(Long id, String productName, int quantity) {
}
```

</details>

### 4.4 `IsolationLevelObservation.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/dto/IsolationLevelObservation.java`
- 역할: 두 번의 조회 결과 비교
- 상세 설명:
- `isolationLevel`, `firstQuantity`, `secondQuantity`를 묶어 실험 결과를 반환합니다.
- `observedSameValue()`는 첫 번째 값과 두 번째 값이 같은지 바로 판단하게 해 줍니다.
- 테스트에서는 이 메서드로 반복 읽기 일관성이 유지됐는지 더 읽기 쉽게 검증합니다.

<details>
<summary><code>IsolationLevelObservation.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.dto;

public record IsolationLevelObservation(String isolationLevel, int firstQuantity, int secondQuantity) {

    public boolean observedSameValue() {
        return firstQuantity == secondQuantity;
    }
}
```

</details>

### 4.5 `IsolationInventoryRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/repository/IsolationInventoryRepository.java`
- 역할: JDBC 기반 재고 저장소
- 상세 설명:
- `@PostConstruct`에서 `mission05_task07_inventory` 테이블을 생성합니다.
- `save()`는 `GeneratedKeyHolder`로 생성된 ID를 읽고, `findById()`와 `findQuantityById()`는 같은 행을 반복 조회하는 실험의 기반이 됩니다.
- `updateQuantity()`는 안쪽 `REQUIRES_NEW` 트랜잭션에서 호출되며, 실제로 독립 커밋되는 변경을 만듭니다.

<details>
<summary><code>IsolationInventoryRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.domain.IsolationInventoryItem;
import jakarta.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class IsolationInventoryRepository {

    private static final String TABLE_NAME = "mission05_task07_inventory";
    private static final RowMapper<IsolationInventoryItem> ITEM_ROW_MAPPER = (resultSet, rowNum) -> new IsolationInventoryItem(
            resultSet.getLong("id"),
            resultSet.getString("product_name"),
            resultSet.getInt("quantity")
    );

    private final JdbcTemplate jdbcTemplate;

    public IsolationInventoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void initializeTable() {
        jdbcTemplate.execute("""
                create table if not exists mission05_task07_inventory (
                    id bigint generated by default as identity primary key,
                    product_name varchar(100) not null,
                    quantity integer not null
                )
                """);
    }

    public Long save(String productName, int quantity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into " + TABLE_NAME + " (product_name, quantity) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            preparedStatement.setString(1, productName);
            preparedStatement.setInt(2, quantity);
            return preparedStatement;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey(), "task07 재고 ID 생성에 실패했습니다.").longValue();
    }

    public IsolationInventoryItem findById(Long itemId) {
        return jdbcTemplate.query(
                "select id, product_name, quantity from " + TABLE_NAME + " where id = ?",
                ITEM_ROW_MAPPER,
                itemId
        ).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("재고 데이터를 찾을 수 없습니다. id=" + itemId));
    }

    public int findQuantityById(Long itemId) {
        return findById(itemId).quantity();
    }

    public void updateQuantity(Long itemId, int quantity) {
        int updatedCount = jdbcTemplate.update(
                "update " + TABLE_NAME + " set quantity = ? where id = ?",
                quantity,
                itemId
        );

        if (updatedCount == 0) {
            throw new IllegalArgumentException("수정할 재고 데이터를 찾을 수 없습니다. id=" + itemId);
        }
    }

    public void deleteAll() {
        jdbcTemplate.update("delete from " + TABLE_NAME);
    }
}
```

</details>

### 4.6 `IsolationLevelStudyService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/service/IsolationLevelStudyService.java`
- 역할: 격리 수준별 실험 흐름 제공
- 상세 설명:
- 핵심 공개 메서드는 `observeReadCommitted()`, `observeRepeatableRead()`, `findItem()`입니다.
- `observeReadCommitted()`와 `observeRepeatableRead()`는 각각 다른 격리 수준으로 같은 실험 로직을 실행합니다.
- 바깥 트랜잭션은 두 번 읽고, 안쪽 서비스는 `REQUIRES_NEW`로 수량 변경을 커밋합니다. 이 협력 구조가 격리 수준 차이를 드러냅니다.

<details>
<summary><code>IsolationLevelStudyService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.domain.IsolationInventoryItem;
import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.dto.IsolationLevelObservation;
import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.repository.IsolationInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IsolationLevelStudyService {

    private final IsolationInventoryRepository isolationInventoryRepository;
    private final IsolationLevelWriteService isolationLevelWriteService;

    public IsolationLevelStudyService(
            IsolationInventoryRepository isolationInventoryRepository,
            IsolationLevelWriteService isolationLevelWriteService
    ) {
        this.isolationInventoryRepository = isolationInventoryRepository;
        this.isolationLevelWriteService = isolationLevelWriteService;
    }

    public Long createSampleItem(String productName, int quantity) {
        return isolationInventoryRepository.save(productName, quantity);
    }

    @Transactional(
            transactionManager = "task07TransactionManager",
            isolation = Isolation.READ_COMMITTED
    )
    public IsolationLevelObservation observeReadCommitted(Long itemId, int updatedQuantity) {
        return observeQuantityTwice("READ_COMMITTED", itemId, updatedQuantity);
    }

    @Transactional(
            transactionManager = "task07TransactionManager",
            isolation = Isolation.REPEATABLE_READ
    )
    public IsolationLevelObservation observeRepeatableRead(Long itemId, int updatedQuantity) {
        return observeQuantityTwice("REPEATABLE_READ", itemId, updatedQuantity);
    }

    @Transactional(
            transactionManager = "task07TransactionManager",
            readOnly = true
    )
    public IsolationInventoryItem findItem(Long itemId) {
        return isolationInventoryRepository.findById(itemId);
    }

    private IsolationLevelObservation observeQuantityTwice(String isolationLevel, Long itemId, int updatedQuantity) {
        int firstQuantity = isolationInventoryRepository.findQuantityById(itemId);
        isolationLevelWriteService.updateQuantityInNewTransaction(itemId, updatedQuantity);
        int secondQuantity = isolationInventoryRepository.findQuantityById(itemId);
        return new IsolationLevelObservation(isolationLevel, firstQuantity, secondQuantity);
    }
}
```

</details>

### 4.7 `IsolationLevelWriteService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/service/IsolationLevelWriteService.java`
- 역할: 독립 트랜잭션으로 수량 변경 커밋
- 상세 설명:
- 핵심 공개 메서드는 `updateQuantityInNewTransaction()`입니다.
- `@Transactional(propagation = Propagation.REQUIRES_NEW)`로 선언되어 기존 바깥 트랜잭션이 있어도 새로운 물리 트랜잭션을 시작합니다.
- 덕분에 바깥 트랜잭션이 아직 끝나지 않았더라도, 안쪽 update 결과를 먼저 커밋시키고 다시 읽는 실험이 가능합니다.

<details>
<summary><code>IsolationLevelWriteService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.repository.IsolationInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IsolationLevelWriteService {

    private final IsolationInventoryRepository isolationInventoryRepository;

    public IsolationLevelWriteService(IsolationInventoryRepository isolationInventoryRepository) {
        this.isolationInventoryRepository = isolationInventoryRepository;
    }

    @Transactional(
            transactionManager = "task07TransactionManager",
            propagation = Propagation.REQUIRES_NEW
    )
    public void updateQuantityInNewTransaction(Long itemId, int quantity) {
        isolationInventoryRepository.updateQuantity(itemId, quantity);
    }
}
```

</details>

### 4.8 `IsolationLevelStudyServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task07_transaction_isolation_level/IsolationLevelStudyServiceTest.java`
- 역할: 격리 수준별 조회 결과 차이 검증
- 상세 설명:
- `readCommittedAllowsNonRepeatableRead()`는 `READ_COMMITTED`에서 첫 조회 10, 두 번째 조회 25가 되는지 검증합니다.
- `repeatableReadKeepsFirstSnapshot()`는 `REPEATABLE_READ`에서 두 번째 조회도 10으로 유지되는지 검증합니다.
- 두 테스트 모두 마지막에 실제 DB 최종 값이 25로 커밋되어 남았는지도 확인해, “관찰 결과”와 “최종 저장 결과”를 분리해 보장합니다.

<details>
<summary><code>IsolationLevelStudyServiceTest.java</code> 전체 코드</summary>

```java
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
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 트랜잭션 격리 수준(`Isolation`)

- 핵심:
  - `READ_COMMITTED`는 더티 리드는 막지만 비반복 읽기와 팬텀 리드는 발생할 수 있습니다.
  - `REPEATABLE_READ`는 같은 행을 다시 읽을 때 값이 달라지는 비반복 읽기를 막습니다.
- 왜 쓰는가:
  - 데이터 정합성이 더 중요한 흐름에서는 더 높은 격리 수준이 필요할 수 있고, 반대로 동시성과 성능이 더 중요하면 낮은 격리 수준을 선택할 수 있습니다.
  - 이번 태스크처럼 “중간 커밋을 다시 볼 수 있는가”를 기준으로 차이를 이해하면 실무 선택이 쉬워집니다.
- 참고 링크:
  - Spring `Isolation` javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/annotation/Isolation.html
  - Java `Connection` 격리 수준 상수: https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Connection.html

### 5.2 `@Transactional`과 전용 트랜잭션 매니저 지정

- 핵심:
  - `@Transactional`은 메서드 단위로 전파 속성, 격리 수준, 읽기 전용 여부를 선언적으로 설정합니다.
  - `transactionManager` 속성으로 어떤 트랜잭션 매니저를 사용할지 명시할 수 있습니다.
- 왜 쓰는가:
  - 이번 태스크는 `JdbcTemplate`과 JDBC 커넥션 수준 실험이 목적이므로, `DataSourceTransactionManager`를 별도로 지정해 의도를 분명하게 드러냈습니다.
  - 여러 데이터 접근 기술(JPA, JDBC)이 함께 있는 프로젝트에서는 어떤 매니저를 사용할지 명확히 적는 것이 디버깅에 도움이 됩니다.
- 참고 링크:
  - Spring `@Transactional` 레퍼런스: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
  - Spring `Isolation` javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/annotation/Isolation.html

### 5.3 `REQUIRES_NEW`

- 핵심:
  - `REQUIRES_NEW`는 기존 트랜잭션이 있어도 참여하지 않고, 항상 새로운 독립 트랜잭션을 시작합니다.
  - 안쪽 트랜잭션은 바깥 트랜잭션과 별개로 커밋하거나 롤백할 수 있습니다.
- 왜 쓰는가:
  - 이번 태스크에서는 바깥 트랜잭션이 아직 끝나지 않은 상태에서 “중간에 다른 트랜잭션이 커밋된 상황”을 인위적으로 만들기 위해 사용했습니다.
  - 멀티스레드 없이도 격리 수준 차이를 재현할 수 있어 테스트가 단순하고 안정적입니다.
- 참고 링크:
  - Spring Transaction Propagation 레퍼런스: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html
  - Spring `Propagation` javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/annotation/Propagation.html

### 5.4 `JdbcTemplate`

- 핵심:
  - `JdbcTemplate`은 JDBC 커넥션 획득, `PreparedStatement` 실행, 예외 변환 같은 반복 작업을 줄여 줍니다.
  - 스프링이 관리하는 트랜잭션에 자연스럽게 참여할 수 있습니다.
- 왜 쓰는가:
  - 이번 태스크는 JPA 1차 캐시 영향을 피하고 실제 DB 읽기 결과를 직접 확인해야 했기 때문에 `JdbcTemplate`이 적합했습니다.
  - 순수 JDBC보다 코드가 짧고, 트랜잭션 참여는 유지하면서 실험 의도는 더 잘 드러납니다.
- 참고 링크:
  - Spring `JdbcTemplate` javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html

## 6. 실행·검증 방법

```bash
./gradlew bootRun
```

- 애플리케이션 실행 후 확인 방법:
  - 브라우저에서 `http://localhost:8080/h2-console`에 접속합니다.
  - JDBC URL은 `jdbc:h2:mem:mission01`, 사용자명은 `sa`, 비밀번호는 빈 값으로 접속합니다.
  - `mission05_task07_inventory` 테이블이 생성되었는지 확인할 수 있습니다.

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.IsolationLevelStudyServiceTest
```

- 테스트 검증 포인트:
  - `READ_COMMITTED` 테스트는 첫 조회 10, 두 번째 조회 25를 기대합니다.
  - `REPEATABLE_READ` 테스트는 첫 조회 10, 두 번째 조회 10을 기대합니다.
  - 두 경우 모두 최종 커밋된 실제 DB 값은 25여야 합니다.

## 7. 결과 확인 방법

- 성공 기준:
  - `IsolationLevelStudyServiceTest`가 통과해야 합니다.
  - `READ_COMMITTED`에서는 같은 트랜잭션 안에서도 값이 바뀌어 보이고, `REPEATABLE_READ`에서는 처음 읽은 값이 유지되어야 합니다.
  - 테스트 종료 후 최종 DB 값은 25로 남아 있어야 합니다.
- 결과 로그 파일:
  - 파일명: `task07-gradle-test-output.txt`
  - 저장 위치: `docs/mission-05-spring-db/task-07-transaction-isolation-level/task07-gradle-test-output.txt`

## 8. 학습 내용

- 트랜잭션 격리 수준은 “트랜잭션이 언제 시작되고 끝나는가”만 다루는 것이 아니라, 같은 트랜잭션 안에서 다른 커밋을 어느 정도까지 볼 수 있는지도 결정합니다.
- `READ_COMMITTED`는 대부분의 일반 조회에 무난하지만, 같은 행을 두 번 읽는 사이 다른 트랜잭션이 커밋되면 값이 달라질 수 있습니다. 이 점을 모르고 비즈니스 로직을 짜면 계산 기준이 중간에 바뀌는 문제가 생길 수 있습니다.
- `REPEATABLE_READ`는 같은 행을 반복 조회할 때 더 안정적인 읽기 결과를 제공합니다. 다만 격리 수준을 올릴수록 동시성 비용이 커질 수 있으므로, 모든 흐름에 무조건 높게 적용하는 것이 정답은 아닙니다.
- 이번 실험에서 특히 중요한 점은 “트랜잭션 내부 관찰 결과”와 “최종 DB 커밋 결과”가 다를 수 있다는 사실입니다. `REPEATABLE_READ`에서 바깥 트랜잭션은 10을 계속 보더라도, 실제 DB에는 안쪽 트랜잭션이 커밋한 25가 남습니다.
- 또한 격리 수준 테스트를 JPA로 바로 작성하면 영속성 컨텍스트 캐시 때문에 DB 차이 대신 프레임워크 캐시 동작을 보게 될 수 있습니다. 어떤 기술을 선택하느냐도 실험 정확성에 직접 영향을 준다는 점을 확인했습니다.
