# 스프링 핵심 원리 - 기본: 의존성 주입과 테스트를 위한 Mock 객체 사용

이 문서는 `mission-02-spring-core-basic`의 `task-03-mock-object` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-03-mock-object`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object`
- 코드 파일 수(테스트 포함): **5개**

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/domain/MockMember.java` | 도메인 상태와 규칙을 표현하는 모델 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/repository/InMemoryMockMemberRepository.java` | 데이터 저장/조회 추상화 계층 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/repository/MockMemberRepository.java` | 데이터 저장/조회 추상화 계층 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/service/MockMemberService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/MockMemberServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `MockMember.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/domain/MockMember.java`
- 역할: 도메인 상태와 규칙을 표현하는 모델
- 상세 설명:
- 비즈니스에서 다루는 상태를 필드로 표현하고, 필요한 변경 메서드를 제공합니다.
- 엔티티인 경우 JPA 매핑 애너테이션으로 테이블/식별자 전략을 정의합니다.

<details>
<summary><code>MockMember.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.domain;

public class MockMember {

    private Long id;
    private String name;
    private String email;

    public MockMember(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
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

    public void update(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
```

</details>

### 4.2 `InMemoryMockMemberRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/repository/InMemoryMockMemberRepository.java`
- 역할: 데이터 저장/조회 추상화 계층
- 상세 설명:
- 저장소 인터페이스로 데이터 접근을 추상화해 구현 교체 가능성을 확보합니다.
- 도메인 객체의 조회/저장 책임을 서비스에서 분리합니다.

<details>
<summary><code>InMemoryMockMemberRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.repository;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.domain.MockMember;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryMockMemberRepository implements MockMemberRepository {

    private final ConcurrentMap<Long, MockMember> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public MockMember save(MockMember member) {
        Long id = member.getId();
        if (id == null) {
            id = sequence.incrementAndGet();
            member.setId(id);
        }
        store.put(id, member);
        return member;
    }

    @Override
    public Optional<MockMember> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<MockMember> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
```

</details>

### 4.3 `MockMemberRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/repository/MockMemberRepository.java`
- 역할: 데이터 저장/조회 추상화 계층
- 상세 설명:
- 저장소 인터페이스로 데이터 접근을 추상화해 구현 교체 가능성을 확보합니다.
- 도메인 객체의 조회/저장 책임을 서비스에서 분리합니다.

<details>
<summary><code>MockMemberRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.repository;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.domain.MockMember;
import java.util.List;
import java.util.Optional;

public interface MockMemberRepository {
    MockMember save(MockMember member);

    Optional<MockMember> findById(Long id);

    List<MockMember> findAll();

    boolean existsById(Long id);

    void deleteById(Long id);
}
```

</details>

### 4.4 `MockMemberService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/service/MockMemberService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>MockMemberService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.domain.MockMember;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.repository.MockMemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MockMemberService {

    private final MockMemberRepository mockMemberRepository;

    public MockMemberService(MockMemberRepository mockMemberRepository) {
        this.mockMemberRepository = mockMemberRepository;
    }

    public MockMember createMember(String name, String email) {
        MockMember newMember = new MockMember(null, name, email);
        return mockMemberRepository.save(newMember);
    }

    public MockMember findMember(Long id) {
        return mockMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + id));
    }

    public List<MockMember> listMembers() {
        return mockMemberRepository.findAll();
    }

    public MockMember updateMember(Long id, String name, String email) {
        MockMember member = findMember(id);
        member.update(name, email);
        return mockMemberRepository.save(member);
    }

    public void deleteMember(Long id) {
        if (!mockMemberRepository.existsById(id)) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + id);
        }
        mockMemberRepository.deleteById(id);
    }
}
```

</details>

### 4.5 `MockMemberServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/MockMemberServiceTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>MockMemberServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.domain.MockMember;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.repository.MockMemberRepository;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.service.MockMemberService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockMemberServiceTest {

    @Mock
    private MockMemberRepository mockMemberRepository;

    @InjectMocks
    private MockMemberService mockMemberService;

    @Test
    void createMember_usesMockRepositorySave() {
        when(mockMemberRepository.save(any(MockMember.class)))
                .thenAnswer(invocation -> {
                    MockMember member = invocation.getArgument(0);
                    member.setId(1L);
                    return member;
                });

        MockMember created = mockMemberService.createMember("Alice", "alice@example.com");

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo("Alice");
        assertThat(created.getEmail()).isEqualTo("alice@example.com");
        verify(mockMemberRepository).save(any(MockMember.class));
    }

    @Test
    void findMember_returnsMockedData() {
        when(mockMemberRepository.findById(1L))
                .thenReturn(Optional.of(new MockMember(1L, "Bob", "bob@example.com")));

        MockMember found = mockMemberService.findMember(1L);

        assertThat(found.getName()).isEqualTo("Bob");
        assertThat(found.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void listMembers_returnsMockedList() {
        when(mockMemberRepository.findAll()).thenReturn(List.of(
                new MockMember(1L, "Carol", "carol@example.com"),
                new MockMember(2L, "Dave", "dave@example.com")
        ));

        List<MockMember> members = mockMemberService.listMembers();

        assertThat(members).hasSize(2);
        assertThat(members.get(0).getName()).isEqualTo("Carol");
        assertThat(members.get(1).getName()).isEqualTo("Dave");
    }

    @Test
    void updateMember_usesMockedFindAndSave() {
        MockMember existing = new MockMember(1L, "Old Name", "old@example.com");
        when(mockMemberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(mockMemberRepository.save(any(MockMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMember updated = mockMemberService.updateMember(1L, "New Name", "new@example.com");

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        verify(mockMemberRepository).findById(1L);
        verify(mockMemberRepository).save(existing);
    }

    @Test
    void deleteMember_callsMockDeleteWhenExists() {
        when(mockMemberRepository.existsById(1L)).thenReturn(true);

        mockMemberService.deleteMember(1L);

        verify(mockMemberRepository).deleteById(1L);
    }

    @Test
    void deleteMember_throwsExceptionWhenMissing() {
        when(mockMemberRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> mockMemberService.deleteMember(9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id=9");
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **테스트 대역(Mock/Fake)**: 실제 의존성 대신 제어 가능한 대역으로 테스트를 안정화합니다.  
  참고 문서: https://martinfowler.com/articles/mocksArentStubs.html
- **단위 테스트 격리**: 저장소/외부 시스템을 분리해 로직 자체만 검증합니다.  
  공식 문서: https://junit.org/junit5/docs/current/user-guide/

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task03_mock_object*"
```

예상 결과:
- 태스크 관련 테스트가 모두 통과해야 합니다.
- 실패 시 문서의 파일별 코드 블록과 테스트 코드를 함께 확인합니다.

## 7. 결과 확인 방법

- 컨트롤러가 있는 태스크는 API 호출(curl/브라우저)로 응답 구조와 상태 코드를 확인합니다.
- SQL 로그/애스펙트 로그/콘솔 출력이 필요한 태스크는 실행 로그를 함께 확인합니다.
- 필요 시 실행 결과를 캡처해 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- 파일 경로 인덱스를 먼저 확인하면 전체 구조를 빠르게 파악할 수 있습니다.
- 컨트롤러-서비스-저장소(또는 정책/도메인) 흐름을 분리하면 변경 지점을 명확히 관리할 수 있습니다.
- 공식 문서를 기준으로 개념을 확인하면서 코드와 연결하면 실습 재현성이 높아집니다.
