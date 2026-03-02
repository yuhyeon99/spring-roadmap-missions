# Spring MVC 기본 구조 설계

이 문서는 `mission-01-spring-intro`의 `task-02-mvc` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-02-mvc`
- 소스 패키지: `com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc`
- 코드 파일 수(테스트 포함): **8개**
- 주요 API 베이스 경로:
  - `/mission01/task02/members` (MemberController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/controller/MemberController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/domain/Member.java` | 도메인 상태와 규칙을 표현하는 모델 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/dto/MemberRequest.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/dto/MemberResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/repository/InMemoryMemberRepository.java` | 데이터 저장/조회 추상화 계층 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/repository/MemberRepository.java` | 데이터 저장/조회 추상화 계층 |
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/service/MemberService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/MemberServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `MemberController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/controller/MemberController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>MemberController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.controller;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.dto.MemberRequest;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.dto.MemberResponse;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.repository.InMemoryMemberRepository;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.service.MemberService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/mission01/task02/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createMember(@RequestBody MemberRequest request) {
        Member created = memberService.createMember(request.getName(), request.getEmail());
        return MemberResponse.from(created);
    }

    @GetMapping
    public List<MemberResponse> listMembers() {
        return memberService.listMembers().stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MemberResponse findMember(@PathVariable Long id) {
        Member member = memberService.findMember(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        return MemberResponse.from(member);
    }
}
```

</details>

### 4.2 `Member.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/domain/Member.java`
- 역할: 도메인 상태와 규칙을 표현하는 모델
- 상세 설명:
- 비즈니스에서 다루는 상태를 필드로 표현하고, 필요한 변경 메서드를 제공합니다.
- 엔티티인 경우 JPA 매핑 애너테이션으로 테이블/식별자 전략을 정의합니다.

<details>
<summary><code>Member.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain;

public class Member {
    private Long id;
    private final String name;
    private final String email;

    public Member(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Member(String name, String email) {
        this(null, name, email);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

### 4.3 `MemberRequest.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/dto/MemberRequest.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>MemberRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.dto;

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

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/dto/MemberResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>MemberResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.dto;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;

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

### 4.5 `InMemoryMemberRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/repository/InMemoryMemberRepository.java`
- 역할: 데이터 저장/조회 추상화 계층
- 상세 설명:
- 저장소 인터페이스로 데이터 접근을 추상화해 구현 교체 가능성을 확보합니다.
- 도메인 객체의 조회/저장 책임을 서비스에서 분리합니다.

<details>
<summary><code>InMemoryMemberRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.repository;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryMemberRepository implements MemberRepository {

    private final ConcurrentMap<Long, Member> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Member save(Member member) {
        Long id = member.getId();
        if (id == null) {
            id = sequence.incrementAndGet();
            member.setId(id);
        }
        store.put(id, member);
        return member;
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}
```

</details>

### 4.6 `MemberRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/repository/MemberRepository.java`
- 역할: 데이터 저장/조회 추상화 계층
- 상세 설명:
- 저장소 인터페이스로 데이터 접근을 추상화해 구현 교체 가능성을 확보합니다.
- 도메인 객체의 조회/저장 책임을 서비스에서 분리합니다.

<details>
<summary><code>MemberRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.repository;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    List<Member> findAll();
    Optional<Member> findById(Long id);
}
```

</details>

### 4.7 `MemberService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/service/MemberService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>MemberService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.service;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member createMember(String name, String email) {
        Member member = new Member(name, email);
        return memberRepository.save(member);
    }

    public List<Member> listMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> findMember(Long id) {
        return memberRepository.findById(id);
    }
}
```

</details>

### 4.8 `MemberServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/MemberServiceTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>MemberServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.repository.InMemoryMemberRepository;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.service.MemberService;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MemberServiceTest {

    private final MemberService memberService = new MemberService(new InMemoryMemberRepository());

    @Test
    void createAndFindMember() {
        Member created = memberService.createMember("Alice", "alice@example.com");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Alice");

        Member found = memberService.findMember(created.getId()).orElseThrow();
        assertThat(found.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void listMembers() {
        memberService.createMember("Bob", "bob@example.com");
        memberService.createMember("Carol", "carol@example.com");

        List<Member> members = memberService.listMembers();
        assertThat(members).hasSize(2);
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **MVC 패턴**: Controller-Service-Repository 책임 분리로 유지보수성을 높입니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/web/webmvc.html
- **계층형 아키텍처**: 표현 계층과 비즈니스 계층, 데이터 계층을 분리해 변경 영향을 줄입니다.  
  참고 문서: https://martinfowler.com/bliki/PresentationDomainDataLayering.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task02_mvc*"
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
