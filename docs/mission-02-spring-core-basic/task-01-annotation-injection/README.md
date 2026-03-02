# 스프링 핵심 원리 - 기본: 애너테이션을 사용하여 빈 주입하기

이 문서는 `mission-02-spring-core-basic`의 `task-01-annotation-injection` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-01-annotation-injection`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection`
- 코드 파일 수(테스트 포함): **9개**
- 주요 API 베이스 경로:
  - `/mission02/task01/greetings` (GreetingController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/controller/GreetingController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/dto/GreetingResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/policy/FormalGreetingPolicy.java` | 전략(인터페이스/구현) 분리로 확장성을 제공 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/policy/FriendlyGreetingPolicy.java` | 전략(인터페이스/구현) 분리로 확장성을 제공 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/policy/GreetingPolicy.java` | 전략(인터페이스/구현) 분리로 확장성을 제공 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/service/AnnotationGreetingService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/service/NameNormalizer.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/service/NameSanitizer.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/AnnotationGreetingServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `GreetingController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/controller/GreetingController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>GreetingController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.dto.GreetingResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service.AnnotationGreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task01/greetings")
public class GreetingController {

    private final AnnotationGreetingService greetingService;

    public GreetingController(AnnotationGreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping
    public GreetingResponse greet(@RequestParam(required = false) String name) {
        return greetingService.greet(name);
    }
}
```

</details>

### 4.2 `GreetingResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/dto/GreetingResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>GreetingResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.dto;

public class GreetingResponse {

    private final String message;
    private final String selectedPolicy;
    private final String injectionType;

    public GreetingResponse(String message, String selectedPolicy, String injectionType) {
        this.message = message;
        this.selectedPolicy = selectedPolicy;
        this.injectionType = injectionType;
    }

    public String getMessage() {
        return message;
    }

    public String getSelectedPolicy() {
        return selectedPolicy;
    }

    public String getInjectionType() {
        return injectionType;
    }
}
```

</details>

### 4.3 `FormalGreetingPolicy.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/policy/FormalGreetingPolicy.java`
- 역할: 전략(인터페이스/구현) 분리로 확장성을 제공
- 상세 설명:
- 공통 인터페이스와 구현체를 분리해 전략 패턴을 적용합니다.
- 새로운 정책 추가 시 기존 코드 변경을 최소화할 수 있습니다.

<details>
<summary><code>FormalGreetingPolicy.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.policy;

import org.springframework.stereotype.Component;

@Component("formalGreetingPolicy")
public class FormalGreetingPolicy implements GreetingPolicy {

    @Override
    public String createMessage(String name) {
        return "안녕하세요, " + name + "님. 애너테이션 기반 빈 주입이 정상 동작했습니다.";
    }
}
```

</details>

### 4.4 `FriendlyGreetingPolicy.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/policy/FriendlyGreetingPolicy.java`
- 역할: 전략(인터페이스/구현) 분리로 확장성을 제공
- 상세 설명:
- 공통 인터페이스와 구현체를 분리해 전략 패턴을 적용합니다.
- 새로운 정책 추가 시 기존 코드 변경을 최소화할 수 있습니다.

<details>
<summary><code>FriendlyGreetingPolicy.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.policy;

import org.springframework.stereotype.Component;

@Component("friendlyGreetingPolicy")
public class FriendlyGreetingPolicy implements GreetingPolicy {

    @Override
    public String createMessage(String name) {
        return "반가워요, " + name + "님! 오늘도 즐겁게 스프링을 학습해봐요.";
    }
}
```

</details>

### 4.5 `GreetingPolicy.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/policy/GreetingPolicy.java`
- 역할: 전략(인터페이스/구현) 분리로 확장성을 제공
- 상세 설명:
- 공통 인터페이스와 구현체를 분리해 전략 패턴을 적용합니다.
- 새로운 정책 추가 시 기존 코드 변경을 최소화할 수 있습니다.

<details>
<summary><code>GreetingPolicy.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.policy;

public interface GreetingPolicy {

    String createMessage(String name);
}
```

</details>

### 4.6 `AnnotationGreetingService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/service/AnnotationGreetingService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>AnnotationGreetingService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.dto.GreetingResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.policy.GreetingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AnnotationGreetingService {

    private final GreetingPolicy greetingPolicy;
    private final NameNormalizer nameNormalizer;

    @Autowired
    public AnnotationGreetingService(
            @Qualifier("formalGreetingPolicy") GreetingPolicy greetingPolicy,
            NameNormalizer nameNormalizer
    ) {
        this.greetingPolicy = greetingPolicy;
        this.nameNormalizer = nameNormalizer;
    }

    public GreetingResponse greet(String rawName) {
        String normalizedName = nameNormalizer.normalize(rawName);
        String message = greetingPolicy.createMessage(normalizedName);
        return new GreetingResponse(message, "formalGreetingPolicy", "@Autowired + @Inject");
    }
}
```

</details>

### 4.7 `NameNormalizer.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/service/NameNormalizer.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>NameNormalizer.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;

@Component
public class NameNormalizer {

    private final NameSanitizer nameSanitizer;

    @Inject
    public NameNormalizer(NameSanitizer nameSanitizer) {
        this.nameSanitizer = nameSanitizer;
    }

    public String normalize(String rawName) {
        return nameSanitizer.sanitize(rawName);
    }
}
```

</details>

### 4.8 `NameSanitizer.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/service/NameSanitizer.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>NameSanitizer.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service;

import org.springframework.stereotype.Component;

@Component
public class NameSanitizer {

    public String sanitize(String rawName) {
        if (rawName == null) {
            return "손님";
        }

        String normalized = rawName.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? "손님" : normalized;
    }
}
```

</details>

### 4.9 `AnnotationGreetingServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task01_annotation_injection/AnnotationGreetingServiceTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>AnnotationGreetingServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.dto.GreetingResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service.AnnotationGreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AnnotationGreetingServiceTest {

    @Autowired
    private AnnotationGreetingService greetingService;

    @Test
    void greet_usesAutowiredAndInjectInjectedBeans() {
        GreetingResponse response = greetingService.greet("  스프링   학습자  ");

        assertThat(response.getMessage()).isEqualTo("안녕하세요, 스프링 학습자님. 애너테이션 기반 빈 주입이 정상 동작했습니다.");
        assertThat(response.getSelectedPolicy()).isEqualTo("formalGreetingPolicy");
        assertThat(response.getInjectionType()).isEqualTo("@Autowired + @Inject");
    }

    @Test
    void greet_usesFallbackNameWhenInputIsBlank() {
        GreetingResponse response = greetingService.greet("   ");

        assertThat(response.getMessage()).contains("손님");
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **애너테이션 기반 빈 주입**: `@Component`, `@Service`로 빈 등록 후 생성자 주입을 사용합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/annotation-config.html
- **빈 후보 선택 규칙**: 동일 타입 빈이 여러 개면 이름/한정자로 구분합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired-qualifiers.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task01_annotation_injection*"
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
