# 스프링 핵심 원리 - 기본: AOP를 사용하여 애스펙트 구현하기

이 문서는 `mission-02-spring-core-basic`의 `task-09-aop-aspect`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-09-aop-aspect`
- 목표:
  - 커스텀 애너테이션(`@TrackExecution`) 기반 포인트컷으로 애스펙트 적용 범위를 제한한다.
  - `@Around` 어드바이스에서 실행 시간을 측정하고 로그를 남긴다.
  - 테스트에서 AOP 프록시 생성과 애너테이션 대상 메서드 로깅 여부를 검증한다.
- 엔드포인트: `GET /mission02/task09/aspect/demo?topic=...`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Annotation | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/annotation/TrackExecution.java` | 커스텀 애너테이션 선언 |
| Aspect | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/aspect/ExecutionLoggingAspect.java` | 공통 관심사(AOP) 분리 |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/controller/AspectDemoController.java` | 요청 진입점(HTTP 매핑/응답 구성) |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/dto/AspectDemoResponse.java` | 요청/응답 데이터 구조 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/service/AspectDemoService.java` | 비즈니스 로직과 흐름 제어 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/ExecutionLoggingAspectTest.java` | 요구사항 검증 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `@TrackExecution`을 정의해 애스펙트 적용 대상을 명시적으로 표시합니다.
2. `ExecutionLoggingAspect`는 `@Around("@annotation(trackExecution)")`로 대상 메서드 실행 전후 시간을 측정합니다.
3. `AspectDemoService#buildSummary()`에 애너테이션을 부여해 실제 적용 지점을 만들고, `ping()`은 비교군으로 둡니다.
4. 테스트는 AOP 프록시 생성 여부와 애너테이션 대상만 로그가 남는지 동시에 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `TrackExecution.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task09_aop_aspect/annotation/TrackExecution.java`
- 역할: 커스텀 애너테이션 선언
- 상세 설명:
- 선언적 표식을 코드에 남겨 적용 대상을 명확히 지정합니다.
- 런타임 유지(RUNTIME) 설정으로 AOP/리플렉션에서 인식되도록 구성합니다.
- 규칙을 애너테이션으로 통일하면 호출부 가독성이 좋아집니다.

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
- 역할: 공통 관심사(AOP) 분리
- 상세 설명:
- 로깅/측정 같은 공통 관심사를 비즈니스 로직과 분리합니다.
- 포인트컷으로 적용 범위를 선언하고, 어드바이스에서 전후 동작을 정의합니다.
- 애스펙트 적용 여부는 프록시 기반으로 테스트에서 검증할 수 있습니다.

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
- 역할: 요청 진입점(HTTP 매핑/응답 구성)
- 상세 설명:
- 기본 경로: `/mission02/task09/aspect`
- 매핑 메서드: Get /demo;
- 컨트롤러는 입력을 바인딩하고 서비스 결과를 HTTP 응답 규약에 맞춰 반환합니다.

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
- 역할: 요청/응답 데이터 구조
- 상세 설명:
- 요청/응답 전용 타입을 분리해 API 계약을 안정적으로 유지합니다.
- 도메인 객체 직접 노출을 피해서 내부 구조 변경 전파를 줄입니다.
- 컨트롤러와 서비스 사이의 데이터 경계를 명확히 만듭니다.

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
- 역할: 비즈니스 로직과 흐름 제어
- 상세 설명:
- 핵심 공개 메서드: `public class AspectDemoService {,    public String buildSummary(String topic) {,    public String ping() {,`
- 서비스 계층에서 검증, 계산, 상태 변경, 예외 처리를 집중 관리합니다.
- 컨트롤러/저장소 사이의 결합을 줄여 테스트 가능성을 높입니다.

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
- 역할: 요구사항 검증 테스트
- 상세 설명:
- 검증 시나리오: `trackedService_isProxiedBySpringAop,aspect_logsOnlyWhenTrackExecutionAnnotationExists,`
- 정상/예외 흐름을 코드 수준에서 고정해 회귀를 빠르게 감지합니다.
- 요구사항이 바뀌면 테스트부터 수정해 변경 범위를 명확히 확인합니다.

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

- **`@Aspect` / `@Around`**
  - 핵심: 메서드 전후 공통 로직(로깅/측정)을 선언적으로 적용합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/advice.html
- **애너테이션 기반 포인트컷**
  - 핵심: 커스텀 애너테이션으로 적용 대상을 안전하게 제한합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/pointcuts.html

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 API 호출 예시

```bash
curl "http://localhost:8080/mission02/task09/aspect/demo?topic=AOP"
```

확인 포인트:
- 응답 본문 정상 반환
- 콘솔에 `[TASK09-AOP]` 실행 시간 로그 출력

### 6.3 테스트

```bash
./gradlew test --tests "*task09_aop_aspect*"
```

## 7. 결과 확인 방법

- 문서의 호출 예시를 그대로 실행해 상태 코드/응답 본문을 확인합니다.
- 테스트 명령으로 자동 검증 통과 여부를 함께 확인합니다.
- 제출이 필요한 경우 실행 결과를 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- 애너테이션 포인트컷은 적용 범위를 코드에서 명확히 드러내 유지보수성을 높입니다.
- 프록시 생성 여부까지 테스트하면 “동작한다”를 넘어 “왜 동작하는지”를 검증할 수 있습니다.
