# JPA와 스프링 데이터 JPA로 CRUD 구현하기

이 문서는 `mission-01-spring-intro`의 `task-04-jpa` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-04-jpa`
- 소스 패키지: `com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa`
- 코드 파일 수(테스트 포함): **8개**
- 주요 API 베이스 경로:
  - `/mission01/task04/members` (MemberJpaController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/controller/MemberJpaController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/domain/Member.java` | 도메인 상태와 규칙을 표현하는 모델 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/dto/MemberRequest.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/dto/MemberResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/repository/MemberJpaRepository.java` | 데이터 저장/조회 추상화 계층 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/service/MemberJpaService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/resources/application.properties` | 실행 환경, DB, JPA, 로깅 등 애플리케이션 설정 |
| `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/MemberServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `MemberJpaController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/controller/MemberJpaController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>MemberJpaController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.controller;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.dto.MemberRequest;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.dto.MemberResponse;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.service.MemberJpaService;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController("memberJpaController")
@RequestMapping("/mission01/task04/members")
public class MemberJpaController {

    private final MemberJpaService memberService;

    public MemberJpaController(MemberJpaService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@RequestBody MemberRequest request) {
        Member created = memberService.create(request.getName(), request.getEmail());
        return ResponseEntity
                .created(URI.create("/mission01/task04/members/" + created.getId()))
                .body(MemberResponse.from(created));
    }

    @GetMapping
    public List<MemberResponse> list() {
        return memberService.findAll().stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MemberResponse findOne(@PathVariable Long id) {
        return MemberResponse.from(memberService.findById(id));
    }

    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id, @RequestBody MemberRequest request) {
        Member updated = memberService.update(id, request.getName(), request.getEmail());
        return MemberResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }
}
```

</details>

### 4.2 `Member.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/domain/Member.java`
- 역할: 도메인 상태와 규칙을 표현하는 모델
- 상세 설명:
- 비즈니스에서 다루는 상태를 필드로 표현하고, 필요한 변경 메서드를 제공합니다.
- 엔티티인 경우 JPA 매핑 애너테이션으로 테이블/식별자 전략을 정의합니다.

<details>
<summary><code>Member.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    protected Member() {
        // JPA 기본 생성자
    }

    public Member(String name, String email) {
        this.name = name;
        this.email = email;
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

    public void update(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
```

</details>

### 4.3 `MemberRequest.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/dto/MemberRequest.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>MemberRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.dto;

public class MemberRequest {
    private String name;
    private String email;

    public MemberRequest() {
    }

    public MemberRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
```

</details>

### 4.4 `MemberResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/dto/MemberResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>MemberResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.dto;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;

public class MemberResponse {
    private final Long id;
    private final String name;
    private final String email;

    public MemberResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
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
}
```

</details>

### 4.5 `MemberJpaRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/repository/MemberJpaRepository.java`
- 역할: 데이터 저장/조회 추상화 계층
- 상세 설명:
- 저장소 인터페이스로 데이터 접근을 추상화해 구현 교체 가능성을 확보합니다.
- 도메인 객체의 조회/저장 책임을 서비스에서 분리합니다.

<details>
<summary><code>MemberJpaRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.repository;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberJpaRepository extends JpaRepository<Member, Long> {
}
```

</details>

### 4.6 `MemberJpaService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/service/MemberJpaService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>MemberJpaService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.service;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.repository.MemberJpaRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service("memberJpaService")
@Transactional
public class MemberJpaService {

    private final MemberJpaRepository memberRepository;

    public MemberJpaService(MemberJpaRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member create(String name, String email) {
        Member member = new Member(name, email);
        return memberRepository.save(member);
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

    public Member update(Long id, String name, String email) {
        Member member = findById(id);
        member.update(name, email);
        return member;
    }

    public void delete(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }
        memberRepository.deleteById(id);
    }
}
```

</details>

### 4.7 `application.properties`

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

### 4.8 `MemberServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/MemberServiceTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>MemberServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.service.MemberJpaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberJpaService memberService;

    @Test
    void create_read_update_delete() {
        Member created = memberService.create("JPA User", "jpa@example.com");

        Member found = memberService.findById(created.getId());
        assertThat(found.getName()).isEqualTo("JPA User");

        Member updated = memberService.update(created.getId(), "Updated", "updated@example.com");
        assertThat(updated.getName()).isEqualTo("Updated");

        List<Member> members = memberService.findAll();
        assertThat(members).hasSize(1);

        memberService.delete(created.getId());
        assertThat(memberService.findAll()).isEmpty();
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **Spring Data JPA (`JpaRepository`)**: 인터페이스 상속만으로 기본 CRUD를 제공합니다.  
  공식 문서: https://docs.spring.io/spring-data/jpa/reference/
- **JPA 엔티티 매핑**: 객체와 테이블을 매핑하고 식별자 전략을 지정합니다.  
  공식 문서: https://jakarta.ee/specifications/persistence/
- **트랜잭션과 변경 감지**: 트랜잭션 안에서 엔티티 변경 시 커밋 시점에 반영됩니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/data-access/transaction.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task04_jpa*"
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
