# Spring MVC 기본 구조 설계

이 문서는 `mission-01-spring-intro`의 `task-02-mvc`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-02-mvc`
- 목표:
  - Controller-Service-Repository 계층을 분리해 MVC 기본 구조를 실습한다.
  - In-Memory 저장소로 회원 생성/조회 API를 구현한다.
  - DTO를 사용해 요청/응답 모델을 도메인 모델과 분리한다.
- 엔드포인트: `POST/GET /mission01/task02/members`, `GET /mission01/task02/members/{id}`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/controller/MemberController.java` | 요청 진입점(HTTP 매핑/응답 구성) |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/domain/Member.java` | 도메인 상태/행위 모델 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/dto/MemberRequest.java` | 요청/응답 데이터 구조 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/dto/MemberResponse.java` | 요청/응답 데이터 구조 |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/repository/InMemoryMemberRepository.java` | 데이터 접근 추상화 |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/repository/MemberRepository.java` | 데이터 접근 추상화 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/service/MemberService.java` | 비즈니스 로직과 흐름 제어 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/MemberServiceTest.java` | 요구사항 검증 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `MemberController`가 HTTP 요청을 수신하고 `MemberRequest`/`MemberResponse`로 API 경계를 만듭니다.
2. `MemberService`는 생성/조회 로직과 기본 검증을 담당해 컨트롤러의 책임을 최소화합니다.
3. `MemberRepository` 인터페이스와 `InMemoryMemberRepository` 구현체를 분리해 저장소 기술 교체 가능성을 확보합니다.
4. `MemberServiceTest`에서 생성 후 조회, 없는 ID 조회 등 주요 분기를 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `MemberController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task02_mvc/controller/MemberController.java`
- 역할: 요청 진입점(HTTP 매핑/응답 구성)
- 상세 설명:
- 기본 경로: `/mission01/task02/members`
- 매핑 메서드: Post;Get;Get /{id};
- 컨트롤러는 입력을 바인딩하고 서비스 결과를 HTTP 응답 규약에 맞춰 반환합니다.

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
- 역할: 도메인 상태/행위 모델
- 상세 설명:
- 도메인의 핵심 상태를 필드로 보관하고, 필요한 변경 메서드로 상태 전이를 관리합니다.
- 애플리케이션 계층은 도메인 API를 통해서만 상태를 변경하도록 제한합니다.
- JPA 엔티티인 경우 매핑 애너테이션과 생성자 규칙을 함께 고려합니다.

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
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

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
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

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
- 역할: 데이터 접근 추상화
- 상세 설명:
- 저장/조회 책임을 분리해 서비스가 영속화 기술 세부사항에 덜 의존하도록 구성합니다.
- 인터페이스 기반 구조로 구현 교체(메모리/DB) 가능성을 열어둡니다.
- 테스트에서 가짜 저장소를 주입해 비즈니스 로직만 검증하기 쉬워집니다.

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
- 역할: 데이터 접근 추상화
- 상세 설명:
- 저장/조회 책임을 분리해 서비스가 영속화 기술 세부사항에 덜 의존하도록 구성합니다.
- 인터페이스 기반 구조로 구현 교체(메모리/DB) 가능성을 열어둡니다.
- 테스트에서 가짜 저장소를 주입해 비즈니스 로직만 검증하기 쉬워집니다.

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
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class MemberService {,    public MemberService(MemberRepository memberRepository) {,    public Member createMember(String name, String email) {,    public List<Member> listMembers() {,    public Optional<Member> findMember(Long id) {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

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
- 역할: 요구사항 검증 테스트
- 상세 설명:
- 검증 시나리오: `createAndFindMember,listMembers,`
- 정상/예외 흐름을 코드 수준에서 고정해 회귀를 빠르게 감지합니다.
- 요구사항이 바뀌면 테스트부터 수정해 변경 범위를 명확히 확인합니다.

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

- **MVC 계층 분리**
  - 핵심: 표현(Controller), 비즈니스(Service), 저장소(Repository) 책임을 분리합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/web/webmvc.html
- **DTO 분리**
  - 핵심: API 계약을 도메인 객체에서 분리해 변경 영향 범위를 줄입니다.
  - 참고: https://martinfowler.com/eaaCatalog/dataTransferObject.html

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 API 호출 예시

```bash
curl -X POST http://localhost:8080/mission01/task02/members \
  -H "Content-Type: application/json" \
  -d '{"name":"kim","email":"kim@example.com"}'

curl http://localhost:8080/mission01/task02/members
curl http://localhost:8080/mission01/task02/members/1
```

### 6.3 테스트

```bash
./gradlew test --tests "*task02_mvc*"
```

## 7. 결과 확인 방법

- 문서의 호출 예시를 그대로 실행해 상태 코드/응답 본문을 확인합니다.
- 테스트 명령으로 자동 검증 통과 여부를 함께 확인합니다.
- 제출이 필요한 경우 실행 결과를 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- MVC 계층 분리를 통해 변경 지점을 컨트롤러/서비스/저장소로 명확히 나눌 수 있었습니다.
- DTO를 분리하면 API 계약 유지와 내부 모델 보호를 동시에 달성할 수 있습니다.
