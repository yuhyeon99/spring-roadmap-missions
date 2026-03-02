# 스프링 핵심 원리 - 기본: 싱글톤 컨테이너 원리 이해하기

이 문서는 `mission-02-spring-core-basic`의 `task-10-singleton-container` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-10-singleton-container`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container`
- 코드 파일 수(테스트 포함): **5개**
- 주요 API 베이스 경로:
  - `/mission02/task10/singleton` (SingletonContainerController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/controller/SingletonContainerController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/dto/SingletonCheckResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/service/SingletonContainerService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/singleton/SingletonTraceBean.java` | 싱글톤 동작 추적/검증용 빈 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/SingletonContainerServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `SingletonContainerController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/controller/SingletonContainerController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>SingletonContainerController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto.SingletonCheckResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.service.SingletonContainerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task10/singleton")
public class SingletonContainerController {

    private final SingletonContainerService singletonContainerService;

    public SingletonContainerController(SingletonContainerService singletonContainerService) {
        this.singletonContainerService = singletonContainerService;
    }

    @GetMapping("/inspect")
    public SingletonCheckResponse inspect() {
        return singletonContainerService.inspectSingletonBean();
    }
}
```

</details>

### 4.2 `SingletonCheckResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/dto/SingletonCheckResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>SingletonCheckResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto;

public class SingletonCheckResponse {

    private final String firstLookupInstanceId;
    private final String secondLookupInstanceId;
    private final int firstLookupIdentityHash;
    private final int secondLookupIdentityHash;
    private final boolean sameInstance;
    private final int firstCallCount;
    private final int secondCallCount;
    private final String summary;

    public SingletonCheckResponse(
            String firstLookupInstanceId,
            String secondLookupInstanceId,
            int firstLookupIdentityHash,
            int secondLookupIdentityHash,
            boolean sameInstance,
            int firstCallCount,
            int secondCallCount,
            String summary
    ) {
        this.firstLookupInstanceId = firstLookupInstanceId;
        this.secondLookupInstanceId = secondLookupInstanceId;
        this.firstLookupIdentityHash = firstLookupIdentityHash;
        this.secondLookupIdentityHash = secondLookupIdentityHash;
        this.sameInstance = sameInstance;
        this.firstCallCount = firstCallCount;
        this.secondCallCount = secondCallCount;
        this.summary = summary;
    }

    public String getFirstLookupInstanceId() {
        return firstLookupInstanceId;
    }

    public String getSecondLookupInstanceId() {
        return secondLookupInstanceId;
    }

    public int getFirstLookupIdentityHash() {
        return firstLookupIdentityHash;
    }

    public int getSecondLookupIdentityHash() {
        return secondLookupIdentityHash;
    }

    public boolean isSameInstance() {
        return sameInstance;
    }

    public int getFirstCallCount() {
        return firstCallCount;
    }

    public int getSecondCallCount() {
        return secondCallCount;
    }

    public String getSummary() {
        return summary;
    }
}
```

</details>

### 4.3 `SingletonContainerService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/service/SingletonContainerService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>SingletonContainerService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto.SingletonCheckResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.singleton.SingletonTraceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SingletonContainerService {

    private final ApplicationContext applicationContext;

    public SingletonContainerService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public SingletonCheckResponse inspectSingletonBean() {
        SingletonTraceBean firstLookup = applicationContext.getBean(SingletonTraceBean.class);
        SingletonTraceBean secondLookup = applicationContext.getBean(SingletonTraceBean.class);

        int firstCallCount = firstLookup.touch();
        int secondCallCount = secondLookup.touch();

        return new SingletonCheckResponse(
                firstLookup.getInstanceId(),
                secondLookup.getInstanceId(),
                System.identityHashCode(firstLookup),
                System.identityHashCode(secondLookup),
                firstLookup == secondLookup,
                firstCallCount,
                secondCallCount,
                "스프링 컨테이너는 기본 스코프에서 빈을 한 번만 생성하고 재사용합니다."
        );
    }
}
```

</details>

### 4.4 `SingletonTraceBean.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/singleton/SingletonTraceBean.java`
- 역할: 싱글톤 동작 추적/검증용 빈
- 상세 설명:
- 동일 타입 빈 조회 시 동일 인스턴스가 반환되는지 확인합니다.
- 공유 상태 카운트를 통해 싱글톤 특성을 실증합니다.

<details>
<summary><code>SingletonTraceBean.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.singleton;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SingletonTraceBean {

    private final String instanceId = UUID.randomUUID().toString();
    private int callCount = 0;

    public synchronized int touch() {
        callCount++;
        return callCount;
    }

    public synchronized int getCallCount() {
        return callCount;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
```

</details>

### 4.5 `SingletonContainerServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/SingletonContainerServiceTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>SingletonContainerServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto.SingletonCheckResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.service.SingletonContainerService;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.singleton.SingletonTraceBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SingletonContainerServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SingletonContainerService singletonContainerService;

    @Test
    void springContainer_returnsSameSingletonBeanInstance() {
        SingletonTraceBean first = applicationContext.getBean(SingletonTraceBean.class);
        SingletonTraceBean second = applicationContext.getBean(SingletonTraceBean.class);

        assertThat(first).isSameAs(second);
        assertThat(first.getInstanceId()).isEqualTo(second.getInstanceId());
    }

    @Test
    void inspectSingletonBean_confirmsSameInstanceAndSharedState() {
        SingletonCheckResponse response = singletonContainerService.inspectSingletonBean();

        assertThat(response.isSameInstance()).isTrue();
        assertThat(response.getFirstLookupInstanceId()).isEqualTo(response.getSecondLookupInstanceId());
        assertThat(response.getFirstLookupIdentityHash()).isEqualTo(response.getSecondLookupIdentityHash());
        assertThat(response.getSecondCallCount()).isEqualTo(response.getFirstCallCount() + 1);
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **싱글톤 컨테이너**: 스프링 기본 스코프는 싱글톤이며, 동일 빈을 재사용합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html
- **상태 공유 주의점**: 싱글톤은 동시성 관점에서 무상태 설계가 중요합니다.  
  참고 문서: https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task10_singleton_container*"
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
