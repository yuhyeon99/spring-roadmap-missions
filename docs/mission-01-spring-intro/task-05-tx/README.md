# 스프링 트랜잭션 관리 적용하기

이 문서는 `mission-01-spring-intro`의 `task-05-tx`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-05-tx`
- 목표:
  - `@Transactional` 적용 시 정상 저장과 예외 롤백 차이를 검증한다.
  - 실패 전용 API(`POST /fail`)로 롤백 시나리오를 재현한다.
  - 트랜잭션 경계를 서비스 계층에 두고 예외 전파 방식으로 일관성 있는 동작을 확인한다.
- 엔드포인트: `POST /mission01/task05/members`, `POST /mission01/task05/members/fail`, `GET /mission01/task05/members`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/controller/MemberTxController.java` | 요청 진입점(HTTP 매핑/응답 구성) |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/exception/TxSimulationException.java` | 실패 시나리오 표현 예외 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/service/MemberTxService.java` | 비즈니스 로직과 흐름 제어 |
| Config | `src/main/resources/application.properties` | 실행/DB/JPA 설정 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/MemberTxServiceTest.java` | 요구사항 검증 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `MemberTxService` 클래스 레벨에 `@Transactional`을 적용해 쓰기 메서드를 기본 트랜잭션 경계로 묶습니다.
2. `createWithFailure()`는 저장 후 `TxSimulationException`을 던져 롤백 시나리오를 의도적으로 유발합니다.
3. `MemberTxController`는 정상 저장 API와 실패 유도 API를 분리해 비교 실험이 가능하도록 구성합니다.
4. `MemberTxServiceTest`는 정상 커밋과 예외 롤백 두 흐름을 각각 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `MemberTxController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/controller/MemberTxController.java`
- 역할: 요청 진입점(HTTP 매핑/응답 구성)
- 상세 설명:
- 기본 경로: `/mission01/task05/members`
- 매핑 메서드: Post;Post /fail;Get;Get /{id};
- 컨트롤러는 입력을 바인딩하고 서비스 결과를 HTTP 응답 규약에 맞춰 반환합니다.

<details>
<summary><code>MemberTxController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.controller;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.dto.MemberRequest;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.dto.MemberResponse;
import com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.service.MemberTxService;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("memberTxController")
@RequestMapping("/mission01/task05/members")
public class MemberTxController {

    private final MemberTxService memberTxService;

    public MemberTxController(MemberTxService memberTxService) {
        this.memberTxService = memberTxService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@RequestBody MemberRequest request) {
        Member created = memberTxService.create(request.getName(), request.getEmail());
        return ResponseEntity
                .created(URI.create("/mission01/task05/members/" + created.getId()))
                .body(MemberResponse.from(created));
    }

    @PostMapping("/fail")
    public ResponseEntity<MemberResponse> createFail(@RequestBody MemberRequest request) {
        memberTxService.createWithFailure(request.getName(), request.getEmail());
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping
    public List<MemberResponse> list() {
        return memberTxService.findAll().stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MemberResponse findOne(@PathVariable Long id) {
        return MemberResponse.from(memberTxService.findById(id));
    }
}
```

</details>

### 4.2 `TxSimulationException.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/exception/TxSimulationException.java`
- 역할: 실패 시나리오 표현 예외
- 상세 설명:
- 예외 타입으로 실패 의도를 명시해 호출부에서 처리 전략을 분기하기 쉽게 만듭니다.
- 트랜잭션 실습에서는 롤백 유도 트리거로 활용됩니다.
- 의미 있는 메시지로 디버깅/로그 분석 효율을 높입니다.

<details>
<summary><code>TxSimulationException.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class TxSimulationException extends RuntimeException {
    public TxSimulationException(String message) {
        super(message);
    }
}
```

</details>

### 4.3 `MemberTxService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/service/MemberTxService.java`
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class MemberTxService {,    public MemberTxService(MemberJpaRepository memberRepository) {,    public Member create(String name, String email) {,    public Member createWithFailure(String name, String email) {,    public List<Member> findAll() {,    public Member findById(Long id) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

<details>
<summary><code>MemberTxService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.service;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.repository.MemberJpaRepository;
import com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.exception.TxSimulationException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service("memberTxService")
@Transactional
public class MemberTxService {

    private final MemberJpaRepository memberRepository;

    public MemberTxService(MemberJpaRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member create(String name, String email) {
        Member member = new Member(name, email);
        return memberRepository.save(member);
    }

    public Member createWithFailure(String name, String email) {
        Member member = new Member(name, email);
        memberRepository.save(member);
        throw new TxSimulationException("트랜잭션 롤백 시뮬레이션");
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }
}
```

</details>

### 4.4 `application.properties`

- 파일 경로: `src/main/resources/application.properties`
- 역할: 실행/DB/JPA 설정
- 상세 설명:
- 개발/학습 환경에 필요한 설정을 코드와 분리해 관리합니다.
- DB 연결, JPA 동작, SQL 로그, 콘솔 접근 경로를 한 곳에서 제어합니다.
- 실행 시점 관찰성을 높여 기능 검증 속도를 개선합니다.

<details>
<summary><code>application.properties</code> 전체 코드</summary>

```properties
spring.application.name=core

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

### 4.5 `MemberTxServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/MemberTxServiceTest.java`
- 역할: 요구사항 검증 테스트
- 상세 설명:
- 검증 시나리오: `success_commits_transaction,runtime_exception_rolls_back,`
- 정상/예외 흐름을 코드 수준에서 고정해 회귀를 빠르게 감지합니다.
- 요구사항이 바뀌면 테스트부터 수정해 변경 범위를 명확히 확인합니다.

<details>
<summary><code>MemberTxServiceTest.java</code> 전체 코드</summary>

```java
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
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **`@Transactional` 선언적 트랜잭션**
  - 핵심: 메서드 단위로 커밋/롤백 경계를 선언합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
- **롤백 규칙**
  - 핵심: 런타임 예외 발생 시 기본 롤백되어 데이터 일관성을 보장합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 API 호출 예시

정상 저장:

```bash
curl -i -X POST http://localhost:8080/mission01/task05/members \
  -H "Content-Type: application/json" \
  -d '{"name":"tx-ok","email":"ok@example.com"}'
```

실패(롤백) 시나리오:

```bash
curl -i -X POST http://localhost:8080/mission01/task05/members/fail \
  -H "Content-Type: application/json" \
  -d '{"name":"tx-fail","email":"fail@example.com"}'

curl http://localhost:8080/mission01/task05/members
```

### 6.3 테스트

```bash
./gradlew test --tests "*task05_tx*"
```

## 7. 결과 확인 방법

- 정상 저장 요청 후 목록 조회 시 데이터가 존재하는지 확인합니다.
- 실패 API 호출 후 목록 조회 시 직전 데이터가 남지 않는지(롤백) 확인합니다.
- 필요 시 `img.png`처럼 실행 결과 캡처를 문서 디렉토리에 보관합니다.

## 8. 학습 내용

- 트랜잭션 경계를 서비스 계층에 두면 비즈니스 규칙 단위로 데이터 정합성을 보장할 수 있습니다.
- 실패를 의도적으로 재현해보면 롤백 규칙과 예외 전파 동작을 명확히 이해할 수 있습니다.
