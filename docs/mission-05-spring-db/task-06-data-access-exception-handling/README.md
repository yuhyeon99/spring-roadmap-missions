# 예외 처리를 통한 데이터 접근 안정성 강화

이 문서는 `mission-05-spring-db`의 `task-06-data-access-exception-handling` 구현 결과를 정리한 보고서입니다. `DataSource` 기반 순수 JDBC 코드에서 발생할 수 있는 `SQLException`을 분기 처리하고, 중복 데이터 저장과 잘못된 SQL 실행 상황에서 사용자에게 이해하기 쉬운 메시지를 반환하는 예제를 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-05-spring-db` / `task-06-data-access-exception-handling`
- 목표:
  - Spring Boot 프로젝트에서 `DataSource`와 순수 JDBC API를 사용해 간단한 회원 저장/조회 기능을 구현합니다.
  - 데이터 저장 중 발생하는 `SQLException`을 잡아서 중복 이메일과 일반 SQL 오류를 구분해 처리합니다.
  - 예외 상황에서도 API가 기술적인 스택 트레이스 대신 사용자 친화적인 에러 메시지를 JSON으로 내려주도록 구성합니다.
- 베이스 경로: `/mission05/task06/members`
- 사용 기술: `Spring Boot`, `Spring JDBC`, `H2 Database`, `Jakarta Validation`, `MockMvc`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Config | `build.gradle` | JDBC 학습용 코드를 위해 `spring-boot-starter-jdbc` 의존성을 추가합니다. |
| Config | `src/main/resources/application.properties` | H2 메모리 DB, JPA 공통 설정, H2 콘솔 경로를 정의합니다. |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/controller/JdbcMemberController.java` | 회원 생성, 단건 조회, 목록 조회, SQL 오류 데모 API를 제공합니다. |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/controller/JdbcMemberExceptionHandler.java` | JDBC 예외, 조회 실패, 검증 실패를 공통 JSON 에러 응답으로 변환합니다. |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/service/JdbcMemberService.java` | 컨트롤러 요청을 받아 저장/조회 흐름과 예외 데모 흐름을 조합합니다. |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/repository/JdbcMemberRepository.java` | `Connection`, `PreparedStatement`, `ResultSet`를 직접 사용해 테이블 생성/저장/조회/예외 변환을 처리합니다. |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/domain/JdbcMember.java` | JDBC 조회 결과를 담는 도메인 객체입니다. |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/dto/JdbcMemberCreateRequest.java` | 회원 생성 요청 JSON을 바인딩하고 이름/이메일/등급을 검증합니다. |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/dto/JdbcMemberResponse.java` | 저장/조회 결과를 사용자 응답 JSON으로 변환합니다. |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/dto/JdbcErrorResponse.java` | 에러 코드, 메시지, 상태 코드, 경로, SQLState, 발생 시각을 묶어 반환합니다. |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/exception/JdbcDataAccessException.java` | JDBC 계층에서 변환한 사용자 친화 예외 정보를 담습니다. |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/exception/JdbcMemberNotFoundException.java` | 없는 회원 조회 시 404 응답으로 바꾸기 위한 예외입니다. |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/JdbcMemberControllerTest.java` | 정상 저장/조회, 중복 이메일 예외, 잘못된 SQL 예외를 통합 테스트로 검증합니다. |
| Artifact | `docs/mission-05-spring-db/task-06-data-access-exception-handling/task06-gradle-test-output.txt` | `task06` 테스트 실행 결과를 저장한 콘솔 출력 파일입니다. |
| Artifact | `docs/mission-05-spring-db/task-06-data-access-exception-handling/responses/create-member-success.txt` | 회원 저장 성공 시 실제 HTTP 응답 원문입니다. |
| Artifact | `docs/mission-05-spring-db/task-06-data-access-exception-handling/responses/duplicate-email-error.txt` | 중복 이메일 입력 시 실제 HTTP 409 응답 원문입니다. |
| Artifact | `docs/mission-05-spring-db/task-06-data-access-exception-handling/responses/broken-sql-error.txt` | 잘못된 SQL 실행 시 실제 HTTP 500 응답 원문입니다. |

## 3. 구현 단계와 주요 코드 해설

1. `build.gradle`에 `spring-boot-starter-jdbc`를 추가해 `DataSource`와 JDBC 핵심 API를 명시적으로 사용할 수 있게 했습니다. 기존 H2 설정은 그대로 유지해 별도 외부 DB 없이 바로 실행할 수 있습니다.
2. `JdbcMemberRepository`는 `@PostConstruct`에서 `mission05_task06_members` 테이블을 생성합니다. 이 테이블은 `email` 컬럼에 `unique` 제약을 두어, 중복 저장 시 실제 `SQLException`이 발생하도록 설계했습니다.
3. 저장과 조회는 모두 `Connection`, `PreparedStatement`, `ResultSet`를 직접 사용하도록 구현했습니다. `save()`에서는 생성 키를 읽어 응답으로 돌려주고, `findById()`와 `findAll()`에서는 조회 결과를 `JdbcMember`로 매핑합니다.
4. 예외 처리 핵심은 `translateException()`입니다. SQLState가 `23` 계열이면 무결성 제약 위반으로 보고 `DUPLICATE_EMAIL`과 `409 Conflict`를 반환하고, 그 외에는 `JDBC_PROCESSING_ERROR`와 `500 Internal Server Error`로 처리합니다.
5. 일반적인 `SQLException` 흐름을 학습할 수 있도록 `GET /mission05/task06/members/demo/sql-error` 엔드포인트를 추가했습니다. 이 엔드포인트는 존재하지 않는 컬럼을 조회하는 SQL을 실행해 의도적으로 `42S22` 계열 오류를 발생시킵니다.
6. `JdbcMemberExceptionHandler`는 JDBC 예외 외에도 `MethodArgumentNotValidException`, `JdbcMemberNotFoundException`을 공통 형식의 JSON으로 변환합니다. 덕분에 정상 응답과 에러 응답의 구조가 분리되어도 클라이언트가 일관성 있게 처리할 수 있습니다.

### 예외 발생 시나리오

1. 중복 이메일 저장
   - 같은 이메일을 두 번 저장하면 H2의 `unique` 제약이 동작해 SQLState `23505`가 발생합니다.
   - 저장소는 이를 `DUPLICATE_EMAIL` 예외로 바꾸고, 최종 응답은 `409`와 `"이미 등록된 이메일입니다. 다른 이메일을 입력해주세요."` 메시지로 내려갑니다.
2. 잘못된 SQL 실행
   - `missing_column`처럼 존재하지 않는 컬럼을 조회하면 SQLState `42S22`가 발생합니다.
   - 저장소는 이를 일반 JDBC 처리 오류로 바꾸고, 최종 응답은 `500`과 `"잘못된 SQL 실행으로 데이터를 조회하지 못했습니다. SQL 문과 컬럼명을 다시 확인해주세요."` 메시지로 내려갑니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `build.gradle`

- 파일 경로: `build.gradle`
- 역할: JDBC 학습용 의존성 추가
- 상세 설명:
- `spring-boot-starter-jdbc`를 추가해 `DataSource`와 JDBC 코드 사용 의도를 명확히 드러냅니다.
- 기존 `spring-boot-starter-data-jpa`, `h2` 설정과 함께 동작하므로 같은 메모리 DB를 여러 태스크가 공유할 수 있습니다.
- 테스트는 `spring-boot-starter-test`와 `MockMvc` 기반으로 유지합니다.

<details>
<summary><code>build.gradle</code> 전체 코드</summary>

```groovy
plugins {
	id 'java'
	id 'org.springframework.boot' version '4.0.2'
	id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.goorm'
version = '0.0.1-SNAPSHOT'
description = 'Demo project for Spring Boot'

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-validation'
	implementation 'org.springframework.boot:spring-boot-starter'
	implementation 'org.springframework.boot:spring-boot-starter-aspectj'
	implementation 'org.springframework.boot:spring-boot-starter-jdbc'
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.1'
	implementation 'jakarta.inject:jakarta.inject-api:2.0.1'
	runtimeOnly 'com.h2database:h2'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

</details>

### 4.2 `application.properties`

- 파일 경로: `src/main/resources/application.properties`
- 역할: 공통 H2/JPA 실행 환경 설정
- 상세 설명:
- `spring.datasource.url`은 H2 메모리 DB를 사용하게 하고, `DB_CLOSE_DELAY=-1`로 애플리케이션이 살아 있는 동안 데이터베이스를 유지합니다.
- 이번 태스크의 JDBC 저장소도 같은 `DataSource`를 주입받아 이 설정을 그대로 사용합니다.
- H2 콘솔이 켜져 있어 필요하면 브라우저에서 테이블 상태를 직접 확인할 수 있습니다.

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

### 4.3 `JdbcMemberController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/controller/JdbcMemberController.java`
- 역할: JDBC 회원 API 진입점
- 상세 설명:
- 기본 경로는 `/mission05/task06/members`입니다.
- `POST /mission05/task06/members`는 회원 저장 후 `201 Created`와 `Location` 헤더를 반환합니다.
- `GET /mission05/task06/members/demo/sql-error`는 교육용으로 일부러 잘못된 SQL을 실행해 예외 처리 흐름을 확인하게 해 줍니다.

<details>
<summary><code>JdbcMemberController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.controller;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto.JdbcMemberCreateRequest;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto.JdbcMemberResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.service.JdbcMemberService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission05/task06/members")
public class JdbcMemberController {

    private final JdbcMemberService jdbcMemberService;

    public JdbcMemberController(JdbcMemberService jdbcMemberService) {
        this.jdbcMemberService = jdbcMemberService;
    }

    @PostMapping
    public ResponseEntity<JdbcMemberResponse> create(@RequestBody @Valid JdbcMemberCreateRequest request) {
        JdbcMemberResponse response = jdbcMemberService.register(
                request.getName(),
                request.getEmail(),
                request.getGrade()
        );
        return ResponseEntity
                .created(URI.create("/mission05/task06/members/" + response.getId()))
                .body(response);
    }

    @GetMapping("/demo/sql-error")
    public void sqlErrorDemo() {
        jdbcMemberService.demonstrateSqlException();
    }

    @GetMapping("/{id}")
    public JdbcMemberResponse findById(@PathVariable Long id) {
        return jdbcMemberService.findById(id);
    }

    @GetMapping
    public List<JdbcMemberResponse> findAll() {
        return jdbcMemberService.findAll();
    }
}
```

</details>

### 4.4 `JdbcMemberExceptionHandler.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/controller/JdbcMemberExceptionHandler.java`
- 역할: JDBC 관련 예외를 공통 JSON 응답으로 변환
- 상세 설명:
- `JdbcDataAccessException`은 저장소에서 이미 사용자 메시지와 상태 코드가 정리된 예외이므로 그대로 응답 바디에 넣습니다.
- 검증 실패는 `400`, 없는 회원 조회는 `404`, JDBC 처리 실패는 `409` 또는 `500`으로 구분됩니다.
- 응답에는 `sqlState`와 `occurredAt`을 포함해 학습 과정에서 어떤 예외가 났는지 추적하기 쉽게 했습니다.

<details>
<summary><code>JdbcMemberExceptionHandler.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.controller;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto.JdbcErrorResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception.JdbcDataAccessException;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception.JdbcMemberNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = JdbcMemberController.class)
public class JdbcMemberExceptionHandler {

    @ExceptionHandler(JdbcDataAccessException.class)
    public ResponseEntity<JdbcErrorResponse> handleJdbcDataAccessException(
            JdbcDataAccessException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(exception.getStatus())
                .body(buildErrorResponse(
                        exception.getErrorCode(),
                        exception.getUserMessage(),
                        exception.getStatus(),
                        request.getRequestURI(),
                        exception.getSqlState()
                ));
    }

    @ExceptionHandler(JdbcMemberNotFoundException.class)
    public ResponseEntity<JdbcErrorResponse> handleNotFoundException(
            JdbcMemberNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        "MEMBER_NOT_FOUND",
                        exception.getMessage(),
                        HttpStatus.NOT_FOUND,
                        request.getRequestURI(),
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JdbcErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        FieldError firstError = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);
        String message = firstError != null
                ? firstError.getDefaultMessage()
                : "입력값을 다시 확인해주세요.";

        return ResponseEntity.badRequest()
                .body(buildErrorResponse(
                        "INVALID_REQUEST",
                        message,
                        HttpStatus.BAD_REQUEST,
                        request.getRequestURI(),
                        null
                ));
    }

    private JdbcErrorResponse buildErrorResponse(
            String errorCode,
            String message,
            HttpStatus status,
            String path,
            String sqlState
    ) {
        return new JdbcErrorResponse(
                errorCode,
                message,
                status.value(),
                path,
                sqlState,
                OffsetDateTime.now().toString()
        );
    }
}
```

</details>

### 4.5 `JdbcMemberService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/service/JdbcMemberService.java`
- 역할: JDBC 저장소와 컨트롤러 사이의 흐름 조합
- 상세 설명:
- 핵심 공개 메서드는 `register`, `findById`, `findAll`, `demonstrateSqlException`입니다.
- 저장과 조회 결과를 `JdbcMemberResponse`로 변환해 컨트롤러가 도메인 객체에 직접 의존하지 않게 했습니다.
- 없는 회원은 `JdbcMemberNotFoundException`으로 바꿔 `404` 처리 흐름으로 넘깁니다.

<details>
<summary><code>JdbcMemberService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.domain.JdbcMember;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto.JdbcMemberResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception.JdbcMemberNotFoundException;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.repository.JdbcMemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JdbcMemberService {

    private final JdbcMemberRepository jdbcMemberRepository;

    public JdbcMemberService(JdbcMemberRepository jdbcMemberRepository) {
        this.jdbcMemberRepository = jdbcMemberRepository;
    }

    public JdbcMemberResponse register(String name, String email, String grade) {
        JdbcMember member = jdbcMemberRepository.save(name, email, grade);
        return JdbcMemberResponse.from(member);
    }

    public JdbcMemberResponse findById(Long id) {
        return jdbcMemberRepository.findById(id)
                .map(JdbcMemberResponse::from)
                .orElseThrow(() -> new JdbcMemberNotFoundException(id));
    }

    public List<JdbcMemberResponse> findAll() {
        return jdbcMemberRepository.findAll()
                .stream()
                .map(JdbcMemberResponse::from)
                .toList();
    }

    public void demonstrateSqlException() {
        jdbcMemberRepository.executeBrokenSelect();
    }
}
```

</details>

### 4.6 `JdbcMemberRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/repository/JdbcMemberRepository.java`
- 역할: 순수 JDBC로 테이블 생성, 저장, 조회, 예외 변환 수행
- 상세 설명:
- `@PostConstruct`에서 테이블을 준비하고, 이후 모든 DB 작업은 `try-with-resources`로 커넥션과 스테이트먼트를 안전하게 닫습니다.
- `save()`는 `RETURN_GENERATED_KEYS`로 생성된 ID를 읽고, `findById()`와 `findAll()`은 `ResultSet`을 `JdbcMember`로 매핑합니다.
- `translateException()`은 SQLState를 보고 무결성 위반과 일반 JDBC 오류를 구분해 `JdbcDataAccessException`으로 감쌉니다.

<details>
<summary><code>JdbcMemberRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.domain.JdbcMember;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception.JdbcDataAccessException;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMemberRepository {

    private static final String TABLE_NAME = "mission05_task06_members";

    private final DataSource dataSource;

    public JdbcMemberRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    void initializeTable() {
        String sql = """
                create table if not exists mission05_task06_members (
                    id bigint generated by default as identity primary key,
                    name varchar(100) not null,
                    email varchar(255) not null unique,
                    grade varchar(50) not null
                )
                """;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new IllegalStateException("task06 JDBC 테이블 초기화에 실패했습니다.", exception);
        }
    }

    public JdbcMember save(String name, String email, String grade) {
        String sql = """
                insert into mission05_task06_members (name, email, grade)
                values (?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, grade);
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new JdbcMember(generatedKeys.getLong(1), name, email, grade);
                }
            }

            throw new JdbcDataAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "KEY_GENERATION_FAILED",
                    "회원 저장은 완료되었지만 생성된 ID를 확인하지 못했습니다.",
                    null,
                    null
            );
        } catch (SQLException exception) {
            throw translateException(
                    exception,
                    "이미 등록된 이메일입니다. 다른 이메일을 입력해주세요.",
                    "회원 저장 중 데이터베이스 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );
        }
    }

    public Optional<JdbcMember> findById(Long id) {
        String sql = """
                select id, name, email, grade
                from mission05_task06_members
                where id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapMember(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException(
                    exception,
                    "회원 조회 중 데이터베이스 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );
        }
    }

    public List<JdbcMember> findAll() {
        String sql = """
                select id, name, email, grade
                from mission05_task06_members
                order by id asc
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            List<JdbcMember> members = new ArrayList<>();
            while (resultSet.next()) {
                members.add(mapMember(resultSet));
            }
            return members;
        } catch (SQLException exception) {
            throw translateException(
                    exception,
                    "회원 목록 조회 중 데이터베이스 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );
        }
    }

    public void executeBrokenSelect() {
        String sql = """
                select missing_column
                from mission05_task06_members
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeQuery();
        } catch (SQLException exception) {
            throw translateException(
                    exception,
                    "잘못된 SQL 실행으로 데이터를 조회하지 못했습니다. SQL 문과 컬럼명을 다시 확인해주세요."
            );
        }
    }

    public void deleteAll() {
        String sql = "delete from " + TABLE_NAME;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw translateException(
                    exception,
                    "테스트 데이터 정리 중 데이터베이스 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );
        }
    }

    private JdbcMember mapMember(ResultSet resultSet) throws SQLException {
        return new JdbcMember(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("grade")
        );
    }

    private JdbcDataAccessException translateException(SQLException exception, String fallbackMessage) {
        return translateException(exception, null, fallbackMessage);
    }

    private JdbcDataAccessException translateException(
            SQLException exception,
            String duplicateMessage,
            String fallbackMessage
    ) {
        if (isIntegrityConstraintViolation(exception) && duplicateMessage != null) {
            return new JdbcDataAccessException(
                    HttpStatus.CONFLICT,
                    "DUPLICATE_EMAIL",
                    duplicateMessage,
                    exception.getSQLState(),
                    exception
            );
        }

        return new JdbcDataAccessException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "JDBC_PROCESSING_ERROR",
                fallbackMessage,
                exception.getSQLState(),
                exception
        );
    }

    private boolean isIntegrityConstraintViolation(SQLException exception) {
        String sqlState = exception.getSQLState();
        return sqlState != null && sqlState.startsWith("23");
    }
}
```

</details>

### 4.7 `JdbcMember.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/domain/JdbcMember.java`
- 역할: JDBC 조회 결과를 담는 도메인 객체
- 상세 설명:
- `id`, `name`, `email`, `grade`만 가진 단순 객체로, JPA 엔터티 없이도 JDBC 결과를 표현할 수 있게 합니다.
- 저장소가 `ResultSet`을 읽은 뒤 이 객체를 만들고, 서비스는 다시 응답 DTO로 변환합니다.

<details>
<summary><code>JdbcMember.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.domain;

public class JdbcMember {

    private final Long id;
    private final String name;
    private final String email;
    private final String grade;

    public JdbcMember(Long id, String name, String email, String grade) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGrade() {
        return grade;
    }
}
```

</details>

### 4.8 `JdbcMemberCreateRequest.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/dto/JdbcMemberCreateRequest.java`
- 역할: 회원 생성 입력값 검증
- 상세 설명:
- `name`, `email`, `grade`를 JSON으로 받아 바인딩합니다.
- `@NotBlank`, `@Email`을 사용해 DB 접근 전에 기본 입력 오류를 걸러냅니다.
- 검증 실패 시 `JdbcMemberExceptionHandler`가 `400` 에러 응답으로 변환합니다.

<details>
<summary><code>JdbcMemberCreateRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class JdbcMemberCreateRequest {

    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    private String name;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    private String email;

    @NotBlank(message = "등급은 비어 있을 수 없습니다.")
    private String grade;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGrade() {
        return grade;
    }
}
```

</details>

### 4.9 `JdbcMemberResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/dto/JdbcMemberResponse.java`
- 역할: 회원 응답 JSON 구조
- 상세 설명:
- 저장소가 반환한 `JdbcMember`를 API 응답용 구조로 바꾸는 역할을 합니다.
- 정적 팩토리 메서드 `from()`을 두어 서비스 코드가 간결해지도록 했습니다.

<details>
<summary><code>JdbcMemberResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.domain.JdbcMember;

public class JdbcMemberResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String grade;

    public JdbcMemberResponse(Long id, String name, String email, String grade) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.grade = grade;
    }

    public static JdbcMemberResponse from(JdbcMember member) {
        return new JdbcMemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getGrade()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGrade() {
        return grade;
    }
}
```

</details>

### 4.10 `JdbcErrorResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/dto/JdbcErrorResponse.java`
- 역할: 에러 응답 JSON 구조
- 상세 설명:
- 클라이언트가 오류를 구분할 수 있도록 `errorCode`, `status`, `sqlState`를 함께 제공합니다.
- `occurredAt`은 학습 과정에서 어떤 시점에 오류가 발생했는지 확인하기 위해 넣었습니다.

<details>
<summary><code>JdbcErrorResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto;

public class JdbcErrorResponse {

    private final String errorCode;
    private final String message;
    private final int status;
    private final String path;
    private final String sqlState;
    private final String occurredAt;

    public JdbcErrorResponse(
            String errorCode,
            String message,
            int status,
            String path,
            String sqlState,
            String occurredAt
    ) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.path = path;
        this.sqlState = sqlState;
        this.occurredAt = occurredAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public String getSqlState() {
        return sqlState;
    }

    public String getOccurredAt() {
        return occurredAt;
    }
}
```

</details>

### 4.11 `JdbcDataAccessException.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/exception/JdbcDataAccessException.java`
- 역할: JDBC 예외를 사용자 메시지와 상태 코드로 감싼 커스텀 예외
- 상세 설명:
- 원본 `SQLException`을 그대로 밖으로 노출하지 않고, 필요한 정보만 안전하게 추려서 전달합니다.
- HTTP 상태 코드와 사용자 메시지를 함께 담아 예외 처리기가 분기 없이 응답을 만들 수 있게 했습니다.

<details>
<summary><code>JdbcDataAccessException.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception;

import org.springframework.http.HttpStatus;

public class JdbcDataAccessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final String userMessage;
    private final String sqlState;

    public JdbcDataAccessException(
            HttpStatus status,
            String errorCode,
            String userMessage,
            String sqlState,
            Throwable cause
    ) {
        super(userMessage, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.sqlState = sqlState;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getSqlState() {
        return sqlState;
    }
}
```

</details>

### 4.12 `JdbcMemberNotFoundException.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/exception/JdbcMemberNotFoundException.java`
- 역할: 없는 회원 조회 시 사용하는 예외
- 상세 설명:
- 데이터가 없을 때도 컨트롤러에서 직접 분기하지 않고 예외 흐름으로 넘기도록 구성했습니다.
- 예외 처리기는 이를 `MEMBER_NOT_FOUND`와 `404`로 변환합니다.

<details>
<summary><code>JdbcMemberNotFoundException.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception;

public class JdbcMemberNotFoundException extends RuntimeException {

    public JdbcMemberNotFoundException(Long id) {
        super("ID가 " + id + "인 회원을 찾을 수 없습니다.");
    }
}
```

</details>

### 4.13 `JdbcMemberControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task06_data_access_exception_handling/JdbcMemberControllerTest.java`
- 역할: 정상/예외 흐름 통합 검증
- 상세 설명:
- `createAndReadMember`는 정상 저장과 목록 조회를 보장합니다.
- `duplicateEmailReturnsFriendlyMessage`는 중복 이메일로 인한 `409`와 사용자 메시지를 검증합니다.
- `brokenSqlReturnsGenericMessage`는 의도적으로 잘못된 SQL을 실행했을 때 `500`과 일반화된 에러 메시지가 내려오는지 확인합니다.

<details>
<summary><code>JdbcMemberControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.repository.JdbcMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class JdbcMemberControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcMemberRepository jdbcMemberRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jdbcMemberRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("정상 요청이면 JDBC로 회원을 저장하고 조회할 수 있다")
    void createAndReadMember() throws Exception {
        mockMvc.perform(post("/mission05/task06/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "김지디비",
                                  "email": "jdbc@example.com",
                                  "grade": "BASIC"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/mission05/task06/members/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("김지디비"))
                .andExpect(jsonPath("$.email").value("jdbc@example.com"))
                .andExpect(jsonPath("$.grade").value("BASIC"));

        mockMvc.perform(get("/mission05/task06/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("jdbc@example.com"));
    }

    @Test
    @DisplayName("중복 이메일로 저장하면 SQLException을 잡아 사용자 친화 메시지를 반환한다")
    void duplicateEmailReturnsFriendlyMessage() throws Exception {
        mockMvc.perform(post("/mission05/task06/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "첫번째회원",
                                  "email": "duplicate@example.com",
                                  "grade": "BASIC"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/mission05/task06/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "두번째회원",
                                  "email": "duplicate@example.com",
                                  "grade": "ADVANCED"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다. 다른 이메일을 입력해주세요."))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/mission05/task06/members"))
                .andExpect(jsonPath("$.sqlState").value("23505"));
    }

    @Test
    @DisplayName("잘못된 SQL을 실행하면 일반 JDBC 오류 메시지를 반환한다")
    void brokenSqlReturnsGenericMessage() throws Exception {
        mockMvc.perform(get("/mission05/task06/members/demo/sql-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("JDBC_PROCESSING_ERROR"))
                .andExpect(jsonPath("$.message").value("잘못된 SQL 실행으로 데이터를 조회하지 못했습니다. SQL 문과 컬럼명을 다시 확인해주세요."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/mission05/task06/members/demo/sql-error"))
                .andExpect(jsonPath("$.sqlState").isNotEmpty());
    }
}
```

</details>

### 4.14 `task06-gradle-test-output.txt`

- 파일 경로: `docs/mission-05-spring-db/task-06-data-access-exception-handling/task06-gradle-test-output.txt`
- 역할: task06 테스트 실행 로그 보관
- 상세 설명:
- `JdbcMemberControllerTest` 단일 테스트 클래스 실행 결과를 저장한 파일입니다.
- `UP-TO-DATE` 여부와 `BUILD SUCCESSFUL` 메시지를 통해 테스트 성공을 빠르게 확인할 수 있습니다.

<details>
<summary><code>task06-gradle-test-output.txt</code> 전체 내용</summary>

```text
Starting a Gradle Daemon, 1 busy Daemon could not be reused, use --status for details
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava UP-TO-DATE
> Task :processTestResources NO-SOURCE
> Task :testClasses UP-TO-DATE
> Task :test UP-TO-DATE

BUILD SUCCESSFUL in 5s
4 actionable tasks: 4 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.0/userguide/configuration_cache_enabling.html
```

</details>

### 4.15 `create-member-success.txt`

- 파일 경로: `docs/mission-05-spring-db/task-06-data-access-exception-handling/responses/create-member-success.txt`
- 역할: 회원 생성 성공 응답 기록
- 상세 설명:
- `POST /mission05/task06/members` 호출 결과를 `-i` 옵션으로 저장한 파일입니다.
- `201 Created`, `Location` 헤더, 응답 JSON 구조를 한 번에 확인할 수 있습니다.

<details>
<summary><code>create-member-success.txt</code> 전체 내용</summary>

```text
HTTP/1.1 201 
Location: /mission05/task06/members/1
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 20 Apr 2026 13:51:16 GMT

{"id":1,"name":"예외처리회원","email":"task06@example.com","grade":"BASIC"}
```

</details>

### 4.16 `duplicate-email-error.txt`

- 파일 경로: `docs/mission-05-spring-db/task-06-data-access-exception-handling/responses/duplicate-email-error.txt`
- 역할: 중복 이메일 예외 응답 기록
- 상세 설명:
- 같은 이메일을 다시 저장했을 때 반환된 `409 Conflict` 응답입니다.
- `errorCode`, `message`, `sqlState`를 통해 어떤 유형의 DB 예외였는지 확인할 수 있습니다.

<details>
<summary><code>duplicate-email-error.txt</code> 전체 내용</summary>

```text
HTTP/1.1 409 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 20 Apr 2026 13:51:35 GMT

{"errorCode":"DUPLICATE_EMAIL","message":"이미 등록된 이메일입니다. 다른 이메일을 입력해주세요.","status":409,"path":"/mission05/task06/members","sqlState":"23505","occurredAt":"2026-04-20T22:51:35.041963+09:00"}
```

</details>

### 4.17 `broken-sql-error.txt`

- 파일 경로: `docs/mission-05-spring-db/task-06-data-access-exception-handling/responses/broken-sql-error.txt`
- 역할: 일반 SQL 오류 응답 기록
- 상세 설명:
- 데모 엔드포인트가 존재하지 않는 컬럼을 조회했을 때 반환된 `500 Internal Server Error` 응답입니다.
- SQLState `42S22`가 포함돼 있어 컬럼 미존재 계열 오류였음을 확인할 수 있습니다.

<details>
<summary><code>broken-sql-error.txt</code> 전체 내용</summary>

```text
HTTP/1.1 500 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 20 Apr 2026 13:51:35 GMT
Connection: close

{"errorCode":"JDBC_PROCESSING_ERROR","message":"잘못된 SQL 실행으로 데이터를 조회하지 못했습니다. SQL 문과 컬럼명을 다시 확인해주세요.","status":500,"path":"/mission05/task06/members/demo/sql-error","sqlState":"42S22","occurredAt":"2026-04-20T22:51:35.845907+09:00"}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 `SQLException`

- 핵심: JDBC 작업 중 발생하는 대표적인 체크 예외로, SQL 문법 오류, 제약 조건 위반, 연결 문제 같은 DB 실패 상황을 담습니다.
- 왜 쓰는가: 순수 JDBC를 사용할 때는 저장소 코드에서 `SQLException`을 직접 받아 적절한 메시지와 상태 코드로 바꿔야 사용자에게 의미 있는 응답을 줄 수 있습니다.
- 참고 링크: [Oracle Javadoc - SQLException](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/SQLException.html)

### 5.2 `DataSource`와 순수 JDBC API

- 핵심: `DataSource`는 커넥션을 제공하고, `Connection`, `PreparedStatement`, `ResultSet`는 실제 SQL 실행과 결과 조회를 담당합니다.
- 왜 쓰는가: ORM 없이도 SQL 실행 과정을 직접 제어할 수 있어 JDBC의 기본 동작과 예외 지점을 학습하기 좋습니다.
- 참고 링크: [Spring Framework Reference - Data Access with JDBC](https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html)

### 5.3 `SQLState` 기반 예외 분기

- 핵심: `SQLException`에는 벤더별 에러 코드 외에도 표준화된 `SQLState`가 들어 있으며, `23` 계열은 무결성 제약 위반처럼 의미 있는 범주를 나타냅니다.
- 왜 쓰는가: 같은 `SQLException`이라도 중복 저장 같은 사용자 수정 가능 오류와 내부 SQL 실수 같은 서버 오류를 구분해 다르게 대응할 수 있습니다.
- 참고 링크: [Oracle Javadoc - SQLException#getSQLState](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/SQLException.html#getSQLState())

### 5.4 `@RestControllerAdvice`

- 핵심: 여러 컨트롤러에서 발생한 예외를 한곳에서 잡아 공통 응답 형식으로 바꾸는 스프링 기능입니다.
- 왜 쓰는가: 컨트롤러마다 `try-catch`를 반복하지 않고도 일관된 에러 JSON을 유지할 수 있습니다.
- 참고 링크: [Spring Framework Reference - Exceptions](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html)

### 5.5 H2 메모리 데이터베이스

- 핵심: 애플리케이션 실행 중 메모리에 생성되는 가벼운 데이터베이스로, 별도 설치 없이 테스트와 학습용 DB 환경을 제공합니다.
- 왜 쓰는가: 이번 태스크처럼 JDBC 저장/조회와 예외 처리 흐름을 빠르게 반복 검증하기 좋습니다.
- 참고 링크: [Spring Boot Reference - SQL Databases](https://docs.spring.io/spring-boot/reference/data/sql.html), [H2 Database Features](https://www.h2database.com/html/features.html)

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

- 예상 결과:
  - 애플리케이션이 `8080` 포트에서 실행됩니다.
  - H2 메모리 DB와 `mission05_task06_members` 테이블이 준비됩니다.

### 6.2 정상 저장 요청

```bash
curl -i -X POST http://localhost:8080/mission05/task06/members \
  -H 'Content-Type: application/json' \
  -d '{"name":"예외처리회원","email":"task06@example.com","grade":"BASIC"}'
```

- 예상 결과:
  - `201 Created`
  - `Location: /mission05/task06/members/{id}`
  - 저장된 회원 JSON 응답

### 6.3 중복 이메일 예외 확인

```bash
curl -i -X POST http://localhost:8080/mission05/task06/members \
  -H 'Content-Type: application/json' \
  -d '{"name":"중복회원","email":"task06@example.com","grade":"ADVANCED"}'
```

- 예상 결과:
  - `409 Conflict`
  - `errorCode=DUPLICATE_EMAIL`
  - `"이미 등록된 이메일입니다. 다른 이메일을 입력해주세요."`

### 6.4 일반 SQL 오류 확인

```bash
curl -i http://localhost:8080/mission05/task06/members/demo/sql-error
```

- 예상 결과:
  - `500 Internal Server Error`
  - `errorCode=JDBC_PROCESSING_ERROR`
  - `"잘못된 SQL 실행으로 데이터를 조회하지 못했습니다. SQL 문과 컬럼명을 다시 확인해주세요."`

### 6.5 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.JdbcMemberControllerTest
```

- 예상 결과:
  - `BUILD SUCCESSFUL`
  - 정상 저장/조회, 중복 이메일, 잘못된 SQL 세 시나리오가 모두 통과합니다.

## 7. 결과 확인 방법

- 성공 기준:
  - 정상 저장 시 `201`과 `Location` 헤더가 반환됩니다.
  - 같은 이메일 재저장 시 `409`와 사용자 친화 메시지가 반환됩니다.
  - 잘못된 SQL 실행 시 `500`과 일반화된 JDBC 오류 메시지가 반환됩니다.
- 저장된 결과 파일:
  - 정상 저장 응답: `docs/mission-05-spring-db/task-06-data-access-exception-handling/responses/create-member-success.txt`
  - 중복 이메일 예외 응답: `docs/mission-05-spring-db/task-06-data-access-exception-handling/responses/duplicate-email-error.txt`
  - 잘못된 SQL 예외 응답: `docs/mission-05-spring-db/task-06-data-access-exception-handling/responses/broken-sql-error.txt`
  - 테스트 실행 로그: `docs/mission-05-spring-db/task-06-data-access-exception-handling/task06-gradle-test-output.txt`

## 8. 학습 내용

- 순수 JDBC를 사용할 때는 SQL 실행 자체보다 예외를 어떻게 정리해서 바깥 계층으로 전달할지가 더 중요합니다. 같은 `SQLException`이라도 중복 데이터 입력처럼 사용자가 수정할 수 있는 문제와, 잘못된 SQL처럼 서버에서 고쳐야 하는 문제를 나눠서 안내해야 합니다.
- `try-with-resources`를 사용하면 커넥션과 스테이트먼트를 안전하게 닫을 수 있어, 예외가 발생한 뒤에도 리소스 누수를 막을 수 있습니다. JDBC는 ORM보다 직접 관리할 요소가 많기 때문에 이런 기본 문법이 특히 중요합니다.
- `SQLState`는 예외를 더 세밀하게 분류하는 힌트입니다. 이번 태스크에서는 `23` 계열을 제약 조건 위반으로 보고 `409`를, 그 외 오류는 `500`으로 분리했습니다. 실제 서비스에서도 이런 기준이 있으면 에러 메시지 품질이 좋아집니다.
- `@RestControllerAdvice`를 사용하면 예외 처리 규칙을 한 파일에 모을 수 있어 컨트롤러 코드가 단순해집니다. 입력값 오류, 조회 실패, JDBC 오류를 각각 분기하더라도 응답 형식은 일관되게 유지할 수 있습니다.
