# 스프링 핵심 원리 - 기본: AOP를 사용하여 애스펙트 구현하기

이 문서는 `mission-02-spring-core-basic`의 `task-09-aop-aspect` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-09-aop-aspect`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect`
- 코드 파일 수(테스트 포함): **6개**
- 주요 API 베이스 경로:
  - `/mission02/task09/aspect` (AspectDemoController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/annotation/TrackExecution.java` | 커스텀 애너테이션 정의 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/aspect/ExecutionLoggingAspect.java` | 공통 관심사(AOP) 로직 분리 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/controller/AspectDemoController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/dto/AspectDemoResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/service/AspectDemoService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/ExecutionLoggingAspectTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `TrackExecution.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/annotation/TrackExecution.java`
- 역할: 커스텀 애너테이션 정의
- 상세 설명:
- 애스펙트 적용 대상처럼 의미 있는 표식을 애너테이션으로 명시합니다.
- 선언적 방식으로 코드 가독성과 재사용성을 높입니다.

<details>
<summary><code>TrackExecution.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackExecution {
}
```

</details>

### 4.2 `ExecutionLoggingAspect.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/aspect/ExecutionLoggingAspect.java`
- 역할: 공통 관심사(AOP) 로직 분리
- 상세 설명:
- 로깅/성능 측정처럼 공통 관심사를 비즈니스 코드와 분리합니다.
- 포인트컷과 어드바이스로 적용 범위를 명확히 제어합니다.

<details>
<summary><code>ExecutionLoggingAspect.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.aspect;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.annotation.TrackExecution;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ExecutionLoggingAspect.class);

    @Around("@annotation(trackExecution)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, TrackExecution trackExecution) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("[TASK09-AOP][{}] executed in {} ms", joinPoint.getSignature().toShortString(), elapsedMs);
        }
    }
}
```

</details>

### 4.3 `AspectDemoController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/controller/AspectDemoController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>AspectDemoController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.dto.AspectDemoResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.service.AspectDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task09/aspect")
public class AspectDemoController {

    private final AspectDemoService aspectDemoService;

    public AspectDemoController(AspectDemoService aspectDemoService) {
        this.aspectDemoService = aspectDemoService;
    }

    @GetMapping("/demo")
    public AspectDemoResponse demo(@RequestParam String topic) {
        String result = aspectDemoService.buildSummary(topic);
        return new AspectDemoResponse(topic, result);
    }
}
```

</details>

### 4.4 `AspectDemoResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/dto/AspectDemoResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>AspectDemoResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.dto;

public class AspectDemoResponse {

    private final String topic;
    private final String result;

    public AspectDemoResponse(String topic, String result) {
        this.topic = topic;
        this.result = result;
    }

    public String getTopic() {
        return topic;
    }

    public String getResult() {
        return result;
    }
}
```

</details>

### 4.5 `AspectDemoService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/service/AspectDemoService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>AspectDemoService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.annotation.TrackExecution;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AspectDemoService {

    @TrackExecution
    public String buildSummary(String topic) {
        String normalizedTopic = normalize(topic);
        return normalizedTopic + " 학습 요청에 대해 애스펙트 로깅이 적용되었습니다.";
    }

    public String ping() {
        return "ok";
    }

    private String normalize(String topic) {
        if (!StringUtils.hasText(topic)) {
            throw new IllegalArgumentException("topic은 필수입니다.");
        }
        return topic.trim().toLowerCase(Locale.ROOT);
    }
}
```

</details>

### 4.6 `ExecutionLoggingAspectTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/ExecutionLoggingAspectTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>ExecutionLoggingAspectTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.service.AspectDemoService;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExecutionLoggingAspectTest {

    @Autowired
    private AspectDemoService aspectDemoService;

    @Test
    void trackedService_isProxiedBySpringAop() {
        assertThat(AopUtils.isAopProxy(aspectDemoService)).isTrue();
    }

    @Test
    void aspect_logsOnlyWhenTrackExecutionAnnotationExists() {
        Logger logger = (Logger) LoggerFactory.getLogger(
                "com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.aspect.ExecutionLoggingAspect"
        );
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            aspectDemoService.buildSummary("AOP");
            aspectDemoService.ping();

            long trackedMethodLogCount = listAppender.list.stream()
                    .filter(event -> event.getFormattedMessage().contains("AspectDemoService.buildSummary"))
                    .count();

            long nonTrackedMethodLogCount = listAppender.list.stream()
                    .filter(event -> event.getFormattedMessage().contains("AspectDemoService.ping"))
                    .count();

            assertThat(trackedMethodLogCount).isGreaterThanOrEqualTo(1);
            assertThat(nonTrackedMethodLogCount).isEqualTo(0);
        } finally {
            logger.detachAppender(listAppender);
        }
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **`@Aspect`와 `@Around`**: 메서드 실행 전후 공통 로직을 캡슐화합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/advice.html
- **애너테이션 기반 포인트컷**: 커스텀 애너테이션으로 적용 대상을 명확히 제한합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/pointcuts.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task09_aop_aspect*"
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
