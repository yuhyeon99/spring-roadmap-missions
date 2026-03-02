# 스프링 트랜잭션 관리 적용하기

이 문서는 `mission-01-spring-intro`의 `task-05-tx` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-05-tx`
- 소스 패키지: `com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx`
- 코드 파일 수(테스트 포함): **5개**
- 주요 API 베이스 경로:
  - `/mission01/task05/members` (MemberTxController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/controller/MemberTxController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/exception/TxSimulationException.java` | 예외 상황을 명확히 표현하는 커스텀 예외 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/service/MemberTxService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/resources/application.properties` | 실행 환경, DB, JPA, 로깅 등 애플리케이션 설정 |
| `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/MemberTxServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `MemberTxController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/controller/MemberTxController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

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
- 역할: 예외 상황을 명확히 표현하는 커스텀 예외
- 상세 설명:
- 비정상 흐름을 명시적 예외 타입으로 분리해 의도를 드러냅니다.
- 상위 계층에서 예외 의미를 바탕으로 적절한 응답 처리를 수행할 수 있습니다.

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
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

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
- 역할: 실행 환경, DB, JPA, 로깅 등 애플리케이션 설정
- 상세 설명:
- 실행 환경에 맞는 설정을 코드 변경 없이 외부화합니다.
- 학습/테스트 반복을 위한 DB/JPA/콘솔 설정을 한 곳에서 관리합니다.

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
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

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

- **`@Transactional`**: 메서드를 하나의 트랜잭션 경계로 관리합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
- **롤백 규칙**: 런타임 예외 발생 시 기본적으로 롤백되어 데이터 정합성을 지킵니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task05_tx*"
```

예상 결과:
- 태스크 관련 테스트가 모두 통과해야 합니다.
- 실패 시 문서의 파일별 코드 블록과 테스트 코드를 함께 확인합니다.

## 7. 결과 확인 방법

- 컨트롤러가 있는 태스크는 API 호출(curl/브라우저)로 응답 구조와 상태 코드를 확인합니다.
- SQL 로그/애스펙트 로그/콘솔 출력이 필요한 태스크는 실행 로그를 함께 확인합니다.
- 현재 태스크 디렉토리의 스크린샷 파일:
  - `img.png`

## 8. 학습 내용

- 파일 경로 인덱스를 먼저 확인하면 전체 구조를 빠르게 파악할 수 있습니다.
- 컨트롤러-서비스-저장소(또는 정책/도메인) 흐름을 분리하면 변경 지점을 명확히 관리할 수 있습니다.
- 공식 문서를 기준으로 개념을 확인하면서 코드와 연결하면 실습 재현성이 높아집니다.
