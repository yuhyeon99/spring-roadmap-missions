# 트랜잭션과 Lazy Loading 연관 테스트

이 문서는 `mission-05-spring-db`의 `task-02-transaction-lazy-loading` 구현 결과를 정리한 보고서입니다. JPA의 지연 로딩이 트랜잭션 경계와 어떤 관계를 가지는지 테스트 코드로 직접 확인하고, `LazyInitializationException`이 언제 발생하는지와 이를 실무적으로 어떻게 피할 수 있는지 정리했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-05-spring-db` / `task-02-transaction-lazy-loading`
- 목표:
  - `FetchType.LAZY`로 설정된 연관 엔터티가 트랜잭션 종료 후 어떤 상태가 되는지 확인한다.
  - 트랜잭션 바깥에서 LAZY 프록시에 접근할 때 `LazyInitializationException`이 발생하는 상황을 재현한다.
  - 트랜잭션 안에서 DTO로 변환하는 방법과 `@EntityGraph`로 필요한 연관 데이터를 미리 조회하는 방법을 비교한다.
- 검증 방식:
  - 서비스 메서드 단위 트랜잭션
  - `@SpringBootTest` 기반 통합 테스트
  - H2 인메모리 DB와 Hibernate 프록시 상태 확인

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/domain/LazyLoadingTeam.java` | 팀 엔터티를 정의하고 `mission05_task02_teams` 테이블에 매핑합니다. |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/domain/LazyLoadingMember.java` | 회원 엔터티를 정의하고 `@ManyToOne(fetch = LAZY)`로 팀과 연관합니다. |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/repository/LazyLoadingTeamRepository.java` | 팀 저장과 테스트 데이터 정리를 담당하는 JPA 리포지토리입니다. |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/repository/LazyLoadingMemberRepository.java` | 기본 조회와 `@EntityGraph` 기반 연관 엔터티 선조회 메서드를 제공합니다. |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/dto/MemberTeamSummary.java` | 트랜잭션 안에서 필요한 값만 꺼내 안전하게 반환하는 DTO입니다. |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/service/TransactionLazyLoadingStudyService.java` | 샘플 데이터 생성, 예외 재현용 조회, 해결 방법용 조회를 분리해 제공합니다. |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/TransactionLazyLoadingStudyServiceTest.java` | 예외 재현, DTO 변환, `@EntityGraph` 해결 시나리오를 통합 테스트로 검증합니다. |
| Config | `src/main/resources/application.properties` | H2, Hibernate DDL 자동 생성, SQL 출력 등 JPA 실험 환경을 설정합니다. |
| Artifact | `docs/mission-05-spring-db/task-02-transaction-lazy-loading/task02-gradle-test-output.txt` | `task02` 테스트 실행 결과를 저장한 콘솔 출력 파일입니다. |

## 3. 구현 단계와 주요 코드 해설

1. `LazyLoadingMember`와 `LazyLoadingTeam` 두 엔터티를 만들고, 회원 쪽 연관관계를 `@ManyToOne(fetch = FetchType.LAZY)`로 명시했습니다. `@ManyToOne`의 기본 fetch 전략은 EAGER이므로, 이번 실험에서는 LAZY를 직접 선언해 프록시 동작을 분명히 드러냈습니다.
2. `TransactionLazyLoadingStudyService#createSample()`은 팀과 회원을 저장하는 짧은 트랜잭션입니다. 이후 테스트는 이 ID를 사용해 같은 데이터를 서로 다른 조회 방식으로 불러옵니다.
3. `findMember()`는 트랜잭션 안에서 회원만 조회한 뒤 엔터티를 그대로 반환합니다. 서비스 메서드가 끝나면 영속성 컨텍스트도 함께 닫히므로, 테스트에서 `member.getTeam().getName()`을 호출하면 초기화되지 않은 LAZY 프록시가 세션 없이 동작하려다 `LazyInitializationException`이 발생합니다.
4. `readMemberTeamSummary()`는 같은 LAZY 연관관계를 사용하되, 트랜잭션 안에서 `member.getTeam().getName()`까지 접근해 DTO로 변환한 뒤 반환합니다. 필요한 값이 이미 단순 문자열로 꺼내졌기 때문에 트랜잭션 밖에서도 안전합니다.
5. `LazyLoadingMemberRepository#findByIdWithTeam()`은 `@EntityGraph(attributePaths = "team")`를 사용해 조회 시점에 팀 연관 엔터티를 함께 로딩합니다. 그래서 `findMemberWithTeam()`이 반환한 엔터티는 트랜잭션이 끝난 뒤에도 팀 이름을 읽을 수 있습니다.
6. 테스트에서는 `Hibernate.isInitialized()`를 함께 사용해 연관 엔터티가 실제로 초기화됐는지 확인했습니다. 즉, 단순히 "예외가 안 났다"가 아니라 프록시 상태 자체를 비교하도록 구성했습니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `LazyLoadingTeam.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/domain/LazyLoadingTeam.java`
- 역할: 팀 엔터티 정의
- 상세 설명:
- `mission05_task02_teams` 테이블에 매핑되며, 팀 이름만 가지는 단순 엔터티입니다.
- 이번 태스크에서는 팀 자체의 로직보다 회원이 참조하는 대상 엔터티 역할이 중요합니다.
- `createSample()`에서 먼저 저장된 뒤 회원 엔터티의 외래 키 대상이 됩니다.

<details>
<summary><code>LazyLoadingTeam.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mission05_task02_teams")
public class LazyLoadingTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    protected LazyLoadingTeam() {
        // JPA 기본 생성자
    }

    public LazyLoadingTeam(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```

</details>

### 4.2 `LazyLoadingMember.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/domain/LazyLoadingMember.java`
- 역할: LAZY 연관관계를 가진 회원 엔터티 정의
- 상세 설명:
- 회원은 `mission05_task02_members` 테이블에 저장되고, `team_id` 외래 키로 팀을 참조합니다.
- `@ManyToOne(fetch = FetchType.LAZY)` 설정이 이번 실험의 핵심입니다. 팀을 바로 조회하지 않고 프록시 객체로 들고 있다가 실제 값이 필요할 때 초기화를 시도합니다.
- 트랜잭션이 살아 있는 동안에는 프록시 초기화가 가능하지만, 종료 뒤에는 세션이 없어 예외가 발생할 수 있습니다.

<details>
<summary><code>LazyLoadingMember.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "mission05_task02_members")
public class LazyLoadingMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private LazyLoadingTeam team;

    protected LazyLoadingMember() {
        // JPA 기본 생성자
    }

    public LazyLoadingMember(String name, LazyLoadingTeam team) {
        this.name = name;
        this.team = team;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LazyLoadingTeam getTeam() {
        return team;
    }
}
```

</details>

### 4.3 `LazyLoadingTeamRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/repository/LazyLoadingTeamRepository.java`
- 역할: 팀 엔터티 저장/삭제용 리포지토리
- 상세 설명:
- `JpaRepository<LazyLoadingTeam, Long>` 상속만으로 기본 저장, 삭제 기능을 사용합니다.
- 별도 커스텀 메서드는 없고, 샘플 데이터 생성과 테스트 정리에서 사용됩니다.
- 팀 저장 책임을 분리해 서비스가 엔터티 생성 흐름을 더 명확히 표현하도록 했습니다.

<details>
<summary><code>LazyLoadingTeamRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LazyLoadingTeamRepository extends JpaRepository<LazyLoadingTeam, Long> {
}
```

</details>

### 4.4 `LazyLoadingMemberRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/repository/LazyLoadingMemberRepository.java`
- 역할: 회원 조회와 연관 엔터티 선조회 처리
- 상세 설명:
- 기본 `findById()`는 팀 연관 엔터티를 초기화하지 않은 상태로 회원만 조회합니다.
- `findByIdWithTeam()`은 `@EntityGraph(attributePaths = "team")`를 사용해 팀까지 함께 로딩합니다.
- 서비스는 같은 회원 조회라도 어떤 사용 목적이냐에 따라 이 리포지토리 메서드를 다르게 선택합니다.

<details>
<summary><code>LazyLoadingMemberRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LazyLoadingMemberRepository extends JpaRepository<LazyLoadingMember, Long> {

    @EntityGraph(attributePaths = "team")
    @Query("select member from LazyLoadingMember member where member.id = :id")
    Optional<LazyLoadingMember> findByIdWithTeam(Long id);
}
```

</details>

### 4.5 `MemberTeamSummary.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/dto/MemberTeamSummary.java`
- 역할: 트랜잭션 안에서 꺼낸 안전한 조회 결과 표현
- 상세 설명:
- 엔터티 자체를 외부로 그대로 넘기지 않고, 테스트에 필요한 값만 담아 반환합니다.
- `memberId`, `memberName`, `teamName` 세 값만 유지하므로 트랜잭션 종료 뒤에도 프록시 초기화가 필요 없습니다.
- 이번 태스크에서 "트랜잭션 내부에서 DTO로 변환" 해결책을 가장 단순하게 보여주는 구조입니다.

<details>
<summary><code>MemberTeamSummary.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.dto;

public record MemberTeamSummary(Long memberId, String memberName, String teamName) {
}
```

</details>

### 4.6 `TransactionLazyLoadingStudyService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/service/TransactionLazyLoadingStudyService.java`
- 역할: 트랜잭션 경계를 기준으로 실험 시나리오를 분리하는 서비스
- 상세 설명:
- 핵심 공개 메서드는 `createSample`, `findMember`, `readMemberTeamSummary`, `findMemberWithTeam`입니다.
- `findMember()`는 예외 재현용 메서드이고, `readMemberTeamSummary()`는 트랜잭션 내부 DTO 변환 해결책입니다.
- `findMemberWithTeam()`은 `@EntityGraph` 쿼리를 사용해 연관 엔터티를 초기화한 뒤 반환합니다.
- 없는 회원 ID가 들어오면 `ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.")`를 던집니다.

<details>
<summary><code>TransactionLazyLoadingStudyService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingMember;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingTeam;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.dto.MemberTeamSummary;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository.LazyLoadingMemberRepository;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository.LazyLoadingTeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionLazyLoadingStudyService {

    private final LazyLoadingMemberRepository memberRepository;
    private final LazyLoadingTeamRepository teamRepository;

    public TransactionLazyLoadingStudyService(
            LazyLoadingMemberRepository memberRepository,
            LazyLoadingTeamRepository teamRepository
    ) {
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public Long createSample(String teamName, String memberName) {
        LazyLoadingTeam team = teamRepository.save(new LazyLoadingTeam(teamName));
        LazyLoadingMember member = memberRepository.save(new LazyLoadingMember(memberName, team));
        return member.getId();
    }

    @Transactional(readOnly = true)
    public LazyLoadingMember findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public MemberTeamSummary readMemberTeamSummary(Long memberId) {
        LazyLoadingMember member = findMember(memberId);
        return new MemberTeamSummary(member.getId(), member.getName(), member.getTeam().getName());
    }

    @Transactional(readOnly = true)
    public LazyLoadingMember findMemberWithTeam(Long memberId) {
        return memberRepository.findByIdWithTeam(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
```

</details>

### 4.7 `TransactionLazyLoadingStudyServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task02_transaction_lazy_loading/TransactionLazyLoadingStudyServiceTest.java`
- 역할: Lazy Loading과 트랜잭션 관계를 검증하는 통합 테스트
- 상세 설명:
- 검증 시나리오는 `lazyAssociationOutsideTransactionThrowsException`, `transactionalDtoMappingAvoidsLazyLoadingException`, `entityGraphPreloadsAssociationBeforeTransactionEnds` 세 가지입니다.
- 첫 번째 테스트는 실패 상황을 그대로 재현하고, 두 번째와 세 번째 테스트는 서로 다른 해결 방향을 보여줍니다.
- `Hibernate.isInitialized()`를 사용해 연관 엔터티 초기화 여부까지 함께 검증합니다.

<details>
<summary><code>TransactionLazyLoadingStudyServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading;

import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingMember;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.dto.MemberTeamSummary;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository.LazyLoadingMemberRepository;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository.LazyLoadingTeamRepository;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.service.TransactionLazyLoadingStudyService;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransactionLazyLoadingStudyServiceTest {

    @Autowired
    private TransactionLazyLoadingStudyService studyService;

    @Autowired
    private LazyLoadingMemberRepository memberRepository;

    @Autowired
    private LazyLoadingTeamRepository teamRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    @DisplayName("트랜잭션이 끝난 뒤 LAZY 연관 엔터티를 접근하면 LazyInitializationException이 발생한다")
    void lazyAssociationOutsideTransactionThrowsException() {
        Long memberId = studyService.createSample("백엔드팀", "김지연");

        LazyLoadingMember member = studyService.findMember(memberId);

        assertThat(Hibernate.isInitialized(member.getTeam())).isFalse();
        assertThatThrownBy(() -> member.getTeam().getName())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    @DisplayName("트랜잭션 안에서 필요한 데이터를 DTO로 변환하면 LAZY 연관 엔터티를 안전하게 사용할 수 있다")
    void transactionalDtoMappingAvoidsLazyLoadingException() {
        Long memberId = studyService.createSample("백엔드팀", "김트랜잭션");

        MemberTeamSummary summary = studyService.readMemberTeamSummary(memberId);

        assertThat(summary.memberId()).isEqualTo(memberId);
        assertThat(summary.memberName()).isEqualTo("김트랜잭션");
        assertThat(summary.teamName()).isEqualTo("백엔드팀");
    }

    @Test
    @DisplayName("EntityGraph로 연관 엔터티를 미리 조회하면 트랜잭션 종료 후에도 필요한 값을 읽을 수 있다")
    void entityGraphPreloadsAssociationBeforeTransactionEnds() {
        Long memberId = studyService.createSample("백엔드팀", "김엔티티그래프");

        LazyLoadingMember member = studyService.findMemberWithTeam(memberId);

        assertThat(Hibernate.isInitialized(member.getTeam())).isTrue();
        assertThat(member.getTeam().getName()).isEqualTo("백엔드팀");
    }
}
```

</details>

### 4.8 `application.properties`

- 파일 경로: `src/main/resources/application.properties`
- 역할: H2 및 JPA 실험 환경 설정
- 상세 설명:
- `spring.jpa.hibernate.ddl-auto=create-drop`으로 테스트 실행 시 테이블을 자동 생성하고 종료 시 정리합니다.
- `spring.jpa.show-sql=true`와 `hibernate.format_sql=true`로 SQL을 관찰할 수 있어 연관 엔터티 로딩 시점을 추적하기 좋습니다.
- 이번 태스크를 위해 별도 속성을 추가하지는 않았고, 기존 공통 학습 환경을 그대로 사용했습니다.

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

### 4.9 `task02-gradle-test-output.txt`

- 파일 경로: `docs/mission-05-spring-db/task-02-transaction-lazy-loading/task02-gradle-test-output.txt`
- 역할: Gradle 테스트 실행 결과 보관
- 상세 설명:
- 문서에서 안내한 테스트 명령을 실제로 실행한 결과를 저장해 둔 파일입니다.
- `BUILD SUCCESSFUL` 여부와 테스트 실행 완료 시점을 텍스트로 확인할 수 있습니다.
- 이미지 대신 콘솔 로그를 남겨 재현 기록으로 사용했습니다.

<details>
<summary><code>task02-gradle-test-output.txt</code> 내용</summary>

```text
> Task :compileJava
> Task :processResources UP-TO-DATE
> Task :classes
> Task :compileTestJava UP-TO-DATE
> Task :processTestResources NO-SOURCE
> Task :testClasses UP-TO-DATE
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-03-25T23:43:13.107+09:00  INFO 11591 --- [core] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
Hibernate: 
    drop table if exists members cascade 
Hibernate: 
    drop table if exists mission05_task01_members cascade 
Hibernate: 
    drop table if exists mission05_task02_members cascade 
Hibernate: 
    drop table if exists mission05_task02_teams cascade 
2026-03-25T23:43:13.110+09:00  INFO 11591 --- [core] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-03-25T23:43:13.111+09:00  INFO 11591 --- [core] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
> Task :test

BUILD SUCCESSFUL in 4s
4 actionable tasks: 2 executed, 2 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.0/userguide/configuration_cache_enabling.html
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 Lazy Loading과 `LazyInitializationException`

- 핵심:
- LAZY 연관관계는 연관 엔터티를 즉시 조회하지 않고 프록시로 들고 있다가, 실제 속성 접근 시점에 DB 조회를 시도합니다.
- 하지만 그 시점에 열린 영속성 컨텍스트(Session)가 없으면 Hibernate는 프록시를 초기화할 수 없어서 `LazyInitializationException`을 던집니다.
- 왜 쓰는가:
- 항상 모든 연관 엔터티를 즉시 조회하면 불필요한 SQL과 메모리 사용이 늘어날 수 있습니다.
- 필요한 순간까지 조회를 미루면 기본 조회 비용을 줄일 수 있고, 대신 어디서 초기화할지 명확하게 설계해야 합니다.
- 참고 링크:
- Hibernate Javadoc - LazyInitializationException: <https://docs.hibernate.org/orm/6.6/javadocs/org/hibernate/LazyInitializationException.html>
- Hibernate ORM User Guide: <https://docs.hibernate.org/orm/current/userguide/html_single/Hibernate_User_Guide.html>

### 5.2 트랜잭션 경계와 `@Transactional`

- 핵심:
- 스프링의 `@Transactional`은 메서드 실행 구간에 트랜잭션 경계를 만들고, JPA에서는 이 구간 동안 영속성 컨텍스트가 함께 유지됩니다.
- LAZY 연관 엔터티 초기화는 이 경계 안에서 일어나야 안전합니다.
- 왜 쓰는가:
- DB 작업의 성공/실패를 한 단위로 묶고, 같은 트랜잭션 안에서 엔터티 변경 감지와 연관 데이터 초기화를 일관되게 처리할 수 있습니다.
- 서비스 계층에 트랜잭션 경계를 두면 컨트롤러나 뷰 계층이 DB 세션에 의존하지 않게 만들 수 있습니다.
- 참고 링크:
- Spring Framework Reference - Transaction Management: <https://docs.spring.io/spring-framework/reference/data-access/transaction.html>
- Spring Data JPA Reference - Transactionality: <https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html>

### 5.3 `@EntityGraph`로 필요한 연관 엔터티만 선조회하기

- 핵심:
- `@EntityGraph`는 특정 조회 메서드에서만 어떤 연관 엔터티를 함께 가져올지 지정하는 방법입니다.
- 이번 태스크에서는 `team`만 미리 로딩해, 트랜잭션이 끝난 뒤에도 팀 이름 조회가 가능하도록 했습니다.
- 왜 쓰는가:
- 연관관계를 전역적으로 EAGER로 바꾸지 않고, 정말 필요한 조회에서만 fetch 계획을 조정할 수 있습니다.
- 조회 목적별로 SQL 전략을 분리할 수 있어 N+1 문제나 과도한 즉시 로딩을 줄이는 데 유용합니다.
- 참고 링크:
- Spring Data JPA Reference - JPA Query Methods: <https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html>
- Spring Data JPA `@EntityGraph` API: <https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/EntityGraph.html>

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

- 예상 결과:
- 스프링 부트 애플리케이션이 `8080` 포트로 실행됩니다.
- H2 콘솔을 사용하려면 `http://localhost:8080/h2-console`에 접속하고 JDBC URL로 `jdbc:h2:mem:mission01`을 사용합니다.

### 6.2 API 호출 또는 화면 접근 방법

```text
이 태스크는 별도 REST API나 화면 대신 테스트 코드로 동작을 검증합니다.
```

- 확인 대상:
- `TransactionLazyLoadingStudyServiceTest`가 서비스 계층 트랜잭션과 JPA 프록시 동작을 직접 검증합니다.
- 첫 번째 테스트는 예외 발생 자체가 성공 기준이고, 두 번째와 세 번째 테스트는 해결 방식이 정상 동작하는지가 기준입니다.

### 6.3 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.TransactionLazyLoadingStudyServiceTest
```

- 예상 결과:
- 3개의 테스트가 모두 통과합니다.
- 콘솔 마지막에 `BUILD SUCCESSFUL`이 출력됩니다.
- HTML 리포트는 `build/reports/tests/test/index.html`에서 확인할 수 있습니다.

## 7. 결과 확인 방법

- 성공 기준:
- `lazyAssociationOutsideTransactionThrowsException()`에서 `LazyInitializationException`이 정확히 발생해야 합니다.
- `transactionalDtoMappingAvoidsLazyLoadingException()`에서는 DTO에 회원명과 팀명이 정상 채워져야 합니다.
- `entityGraphPreloadsAssociationBeforeTransactionEnds()`에서는 `Hibernate.isInitialized(member.getTeam())`가 `true`이고, 팀 이름 조회가 성공해야 합니다.
- 결과 확인 파일:
- 콘솔 실행 결과 파일명: `task02-gradle-test-output.txt`
- 저장 위치: `docs/mission-05-spring-db/task-02-transaction-lazy-loading/task02-gradle-test-output.txt`
- 추가 확인 경로:
- HTML 테스트 리포트: `build/reports/tests/test/index.html`

## 8. 학습 내용

이번 태스크에서 가장 중요한 점은 "LAZY가 문제"가 아니라 "LAZY를 어느 경계에서 초기화할지 설계하지 않으면 문제"라는 사실이었습니다. 엔터티를 서비스 밖으로 그대로 넘긴 뒤 나중에 연관 엔터티를 읽으려 하면, 이미 트랜잭션이 끝난 상태라 Hibernate가 더 이상 DB에 접근할 수 없습니다. 그래서 예외 자체를 없애려면 연관관계를 무조건 EAGER로 바꾸기보다, 필요한 데이터를 서비스 안에서 DTO로 변환하거나 조회 메서드에서만 `@EntityGraph` 같은 명시적 fetch 전략을 사용하는 편이 더 안전합니다.

또 하나 확인한 점은 트랜잭션이 단순히 커밋/롤백만 담당하는 것이 아니라, JPA 엔터티를 다루는 "유효한 작업 구간" 역할도 한다는 점입니다. 같은 회원 조회라도 트랜잭션 안에서 팀 이름까지 읽으면 정상이고, 트랜잭션이 끝난 뒤 프록시를 초기화하려 하면 실패합니다. 결국 서비스 계층에서 어떤 데이터를 어디까지 준비해서 반환할지 결정하는 것이 JPA 사용 품질을 크게 좌우합니다.
