# Mission 06 - Task 08. 스프링 AOP를 사용한 예외 처리 시스템 구현

## 1. 작업 개요
이번 태스크에서는 스프링 AOP를 사용해 예외 발생 시 공통 로직을 실행하는 구조를 구현했습니다. 비즈니스 서비스는 장애 복구 작업을 수행하거나 예외를 던지는 책임만 가지도록 두고, 예외가 발생했을 때의 로깅과 알림 저장은 `@Aspect`가 담당하도록 분리했습니다.

구현 예제는 운영 장애 복구 승인 시나리오입니다. `recoverIncident()`가 실패하면 `@AfterThrowing` advice가 동작해 경고 이력을 저장하고 에러 로그를 남깁니다. 이후 컨트롤러 예외 핸들러가 실패 응답에 최신 알림 정보를 함께 실어 주기 때문에, AOP가 실제로 개입했는지를 API 응답만으로도 확인할 수 있습니다.

## 2. 코드 파일 경로 인덱스
| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Annotation | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/annotation/NotifyOnException.java` | 예외 알림 AOP를 적용할 메서드를 지정하는 커스텀 어노테이션 |
| Aspect | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/aspect/ExceptionAlertAspect.java` | 예외 발생 직후 `@AfterThrowing`으로 로그와 알림 이력을 남기는 Aspect |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/controller/AopExceptionHandlingController.java` | 개념 요약, 복구 실행, 알림 조회 API를 제공하는 컨트롤러 |
| Config | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/controller/AopExceptionHandlingExceptionHandler.java` | 복구 실패/입력 오류를 JSON 응답으로 변환하는 예외 핸들러 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/dto/AopExceptionAlertHistoryResponse.java` | 누적된 예외 알림 목록 응답 DTO |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/dto/AopExceptionErrorResponse.java` | 실패 응답과 최신 알림 정보를 함께 반환하는 에러 DTO |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/dto/AopExceptionRecoveryResponse.java` | 복구 성공 결과를 API 응답 형태로 변환하는 DTO |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/dto/AopExceptionSummaryResponse.java` | AOP 예외 처리 개념 요약 응답 DTO |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/exception/IncidentRecoveryException.java` | 장애 복구 승인 실패를 표현하는 비즈니스 예외 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/service/AopExceptionHandlingService.java` | 성공/실패 복구 시나리오를 제공하는 서비스 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/service/IncidentRecoveryResult.java` | 서비스 내부 복구 결과 모델 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/support/ExceptionAlertEntry.java` | 알림 이력 한 건을 표현하는 모델 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/support/ExceptionAlertStore.java` | AOP가 저장한 알림 이력을 메모리에 보관하는 저장소 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/AopExceptionHandlingControllerTest.java` | 프록시 적용, 예외 알림, API 응답을 검증하는 통합 테스트 |

## 3. 구현 단계와 주요 코드 해설
### 3.1 예외 알림 대상 메서드 지정
`@NotifyOnException` 어노테이션을 만들고, 예외가 발생했을 때 공통 처리하고 싶은 서비스 메서드에 붙였습니다. 어노테이션에 `value`와 `alertTarget`을 두어, 어떤 작업인지와 어디로 경고를 보낼지 메서드 선언부에서 바로 읽을 수 있게 했습니다.

### 3.2 `@AfterThrowing`으로 실패 후처리 분리
`ExceptionAlertAspect`는 `@AfterThrowing("@annotation(notifyOnException)")` 조건으로 동작합니다. 대상 메서드가 예외를 던지면 메서드명, 작업명, 알림 대상, 예외 타입, 메시지를 `ExceptionAlertStore`에 저장하고, `[TASK08-AOP][ALERT]` 형식의 로그를 남깁니다.

이번 태스크의 핵심은 “예외를 잡아서 없애는 것”이 아니라, “예외가 발생한 사실을 공통 방식으로 기록하고 알리는 것”입니다. 그래서 Aspect는 예외를 삼키지 않고 다시 전파하며, 실제 HTTP 상태 코드는 컨트롤러 예외 핸들러가 결정합니다.

### 3.3 서비스와 컨트롤러 역할 분리
`AopExceptionHandlingService`는 입력값 검증 후 정상 복구 시나리오를 반환하거나, `triggerFailure=true`일 때 `IncidentRecoveryException`을 던집니다. 컨트롤러는 다음 세 가지 확인용 API를 제공합니다.

- `GET /mission06/task08/aop-exception-handling/summary`: AOP 예외 처리 개념 요약
- `GET /mission06/task08/aop-exception-handling/incidents/{incidentId}/recovery`: 정상/실패 복구 시나리오 실행
- `GET /mission06/task08/aop-exception-handling/alerts/latest`: AOP가 저장한 최신 알림 이력 조회

### 3.4 예외 응답에 AOP 처리 결과 포함
`AopExceptionHandlingExceptionHandler`는 `IncidentRecoveryException`을 500 응답으로 바꾸면서, `alertCount`와 `latestAlert`를 함께 반환합니다. 이렇게 하면 실패 응답만 확인해도 “서비스에서 예외가 발생했고, 그 전에 AOP가 알림을 남겼다”는 흐름이 드러납니다.

### 3.5 테스트 전략
테스트는 네 가지 축으로 구성했습니다.

- 서비스 빈이 실제로 스프링 AOP 프록시인지 확인
- 예외 발생 시 `ListAppender`로 로그가 기록되는지 확인
- 예외 발생 시 `ExceptionAlertStore`에 알림 이력이 저장되는지 확인
- 성공/실패 API 응답과 알림 조회 API가 기대한 JSON 구조를 반환하는지 확인

## 4. 파일별 상세 설명 + 전체 코드
### 4.1 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/annotation/NotifyOnException.java`
- 역할: 예외 알림 Aspect를 적용할 메서드를 표시합니다.
- 상세 설명: `@Target(ElementType.METHOD)`로 메서드에만 붙일 수 있게 했고, 런타임 유지 정책을 사용해 Aspect가 어노테이션 값을 읽을 수 있게 했습니다. `value`는 작업 이름, `alertTarget`은 알림 대상 채널 역할을 합니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotifyOnException {

    String value() default "";

    String alertTarget() default "slack://ops-alert";
}
```
</details>

### 4.2 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/aspect/ExceptionAlertAspect.java`
- 역할: 예외 발생 직후 공통 로깅과 알림 저장을 수행합니다.
- 상세 설명: `@AfterThrowing`으로 대상 메서드의 예외 종료 지점만 가로챕니다. `JoinPoint`에서 클래스명과 메서드명을 뽑고, 어노테이션에서 작업명과 알림 대상을 읽어 `ExceptionAlertEntry`를 만든 뒤 저장소와 로그에 반영합니다. 예외는 처리하지 않고 그대로 전파합니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.aspect;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.annotation.NotifyOnException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertEntry;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertStore;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionAlertAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionAlertAspect.class);

    private final ExceptionAlertStore exceptionAlertStore;

    public ExceptionAlertAspect(ExceptionAlertStore exceptionAlertStore) {
        this.exceptionAlertStore = exceptionAlertStore;
    }

    @AfterThrowing(
            value = "@annotation(notifyOnException)",
            throwing = "exception"
    )
    public void alertOnFailure(JoinPoint joinPoint, NotifyOnException notifyOnException, Throwable exception) {
        String methodLabel = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "."
                + joinPoint.getSignature().getName();
        String operation = resolveOperationName(notifyOnException, methodLabel);

        ExceptionAlertEntry entry = new ExceptionAlertEntry(
                "AFTER_THROWING",
                operation,
                methodLabel,
                notifyOnException.alertTarget(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );

        exceptionAlertStore.add(entry);
        log.error(
                "[TASK08-AOP][ALERT][{}] {} target={} exception={} message={}",
                operation,
                methodLabel,
                entry.getAlertTarget(),
                entry.getExceptionType(),
                entry.getMessage()
        );
    }

    private String resolveOperationName(NotifyOnException notifyOnException, String methodLabel) {
        if (notifyOnException.value() == null || notifyOnException.value().isBlank()) {
            return methodLabel;
        }
        return notifyOnException.value();
    }
}
```
</details>

### 4.3 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/controller/AopExceptionHandlingController.java`
- 역할: 개념 요약, 복구 실행, 알림 조회 API를 노출합니다.
- 상세 설명: 기본 경로는 `/mission06/task08/aop-exception-handling`입니다. `GET /summary`는 개념 설명을 반환하고, `GET /incidents/{incidentId}/recovery`는 정상/실패 시나리오를 실행하며, `GET /alerts/latest`는 현재 저장된 알림 목록을 반환합니다. `GET /health`는 AOP 비적용 메서드 검증용입니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto.AopExceptionAlertHistoryResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto.AopExceptionRecoveryResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto.AopExceptionSummaryResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service.AopExceptionHandlingService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertStore;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task08/aop-exception-handling")
public class AopExceptionHandlingController {

    private final AopExceptionHandlingService aopExceptionHandlingService;
    private final ExceptionAlertStore exceptionAlertStore;

    public AopExceptionHandlingController(
            AopExceptionHandlingService aopExceptionHandlingService,
            ExceptionAlertStore exceptionAlertStore
    ) {
        this.aopExceptionHandlingService = aopExceptionHandlingService;
        this.exceptionAlertStore = exceptionAlertStore;
    }

    @GetMapping("/summary")
    public AopExceptionSummaryResponse summary() {
        return new AopExceptionSummaryResponse(
                "스프링 AOP는 예외가 발생한 지점을 공통 관심사로 분리해 로깅, 알림, 감사 기록을 한곳에서 처리할 수 있습니다.",
                List.of("Join Point", "Pointcut", "Advice", "AfterThrowing", "Proxy"),
                List.of(
                        "@AfterThrowing은 대상 메서드가 예외를 던진 직후 실행됩니다.",
                        "비즈니스 서비스는 예외를 던지는 책임에 집중하고, 공통 대응은 Aspect가 맡습니다.",
                        "예외 알림 로직을 서비스마다 복붙하지 않아도 되어 변경 비용이 줄어듭니다."
                )
        );
    }

    @GetMapping("/incidents/{incidentId}/recovery")
    public AopExceptionRecoveryResponse recoverIncident(
            @PathVariable String incidentId,
            @RequestParam(defaultValue = "ops-engineer") String operatorId,
            @RequestParam(defaultValue = "false") boolean triggerFailure
    ) {
        return AopExceptionRecoveryResponse.from(
                aopExceptionHandlingService.recoverIncident(incidentId, operatorId, triggerFailure),
                exceptionAlertStore.getEntries().size()
        );
    }

    @GetMapping("/alerts/latest")
    public AopExceptionAlertHistoryResponse latestAlerts() {
        return new AopExceptionAlertHistoryResponse(exceptionAlertStore.getEntries());
    }

    @GetMapping("/health")
    public String health() {
        return aopExceptionHandlingService.healthCheck();
    }
}
```
</details>

### 4.4 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/controller/AopExceptionHandlingExceptionHandler.java`
- 역할: 예외를 HTTP 응답으로 변환합니다.
- 상세 설명: `AopExceptionHandlingController`에만 적용되는 `@RestControllerAdvice`입니다. `IncidentRecoveryException`은 500으로, `IllegalArgumentException`은 400으로 변환합니다. 두 경우 모두 현재 알림 개수와 최신 알림을 응답에 포함해 AOP 실행 여부를 확인할 수 있게 했습니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto.AopExceptionErrorResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.exception.IncidentRecoveryException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AopExceptionHandlingController.class)
public class AopExceptionHandlingExceptionHandler {

    private final ExceptionAlertStore exceptionAlertStore;

    public AopExceptionHandlingExceptionHandler(ExceptionAlertStore exceptionAlertStore) {
        this.exceptionAlertStore = exceptionAlertStore;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IncidentRecoveryException.class)
    public AopExceptionErrorResponse handleIncidentRecoveryFailure(
            IncidentRecoveryException exception,
            HttpServletRequest request
    ) {
        return new AopExceptionErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                exception.getMessage(),
                request.getRequestURI(),
                exceptionAlertStore.getEntries().size(),
                exceptionAlertStore.getLatestEntry().orElse(null)
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public AopExceptionErrorResponse handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new AopExceptionErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI(),
                exceptionAlertStore.getEntries().size(),
                exceptionAlertStore.getLatestEntry().orElse(null)
        );
    }
}
```
</details>

### 4.5 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/dto/AopExceptionAlertHistoryResponse.java`
- 역할: 알림 조회 API 응답을 구성합니다.
- 상세 설명: 현재 저장된 알림 목록과 개수를 함께 반환합니다. 컨트롤러에서 저장소 내용을 바로 노출하지 않고, API 응답 구조를 명시적으로 유지하기 위해 분리했습니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertEntry;
import java.util.List;

public class AopExceptionAlertHistoryResponse {

    private final int count;
    private final List<ExceptionAlertEntry> entries;

    public AopExceptionAlertHistoryResponse(List<ExceptionAlertEntry> entries) {
        this.count = entries.size();
        this.entries = List.copyOf(entries);
    }

    public int getCount() {
        return count;
    }

    public List<ExceptionAlertEntry> getEntries() {
        return entries;
    }
}
```
</details>

### 4.6 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/dto/AopExceptionErrorResponse.java`
- 역할: 실패 응답 본문을 표현합니다.
- 상세 설명: 상태 코드, 에러 이름, 메시지, 요청 경로와 함께 `alertCount`, `latestAlert`를 담습니다. 예외 핸들러가 이 DTO를 반환하므로, 사용자 입장에서는 “실패 원인”과 “AOP 후처리 결과”를 한 번에 볼 수 있습니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertEntry;

public class AopExceptionErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final int alertCount;
    private final ExceptionAlertEntry latestAlert;

    public AopExceptionErrorResponse(
            int status,
            String error,
            String message,
            String path,
            int alertCount,
            ExceptionAlertEntry latestAlert
    ) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.alertCount = alertCount;
        this.latestAlert = latestAlert;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public int getAlertCount() {
        return alertCount;
    }

    public ExceptionAlertEntry getLatestAlert() {
        return latestAlert;
    }
}
```
</details>

### 4.7 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/dto/AopExceptionRecoveryResponse.java`
- 역할: 성공한 복구 실행 결과를 응답으로 변환합니다.
- 상세 설명: 서비스 내부 모델을 API 응답 전용 형태로 매핑합니다. 실행된 단계 목록과 현재 알림 개수를 함께 내려 주어, 성공 흐름에서는 알림이 생기지 않았음을 바로 확인할 수 있습니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service.IncidentRecoveryResult;
import java.util.List;

public class AopExceptionRecoveryResponse {

    private final String incidentId;
    private final String operatorId;
    private final String status;
    private final List<String> executedSteps;
    private final int alertCount;

    public AopExceptionRecoveryResponse(
            String incidentId,
            String operatorId,
            String status,
            List<String> executedSteps,
            int alertCount
    ) {
        this.incidentId = incidentId;
        this.operatorId = operatorId;
        this.status = status;
        this.executedSteps = List.copyOf(executedSteps);
        this.alertCount = alertCount;
    }

    public static AopExceptionRecoveryResponse from(IncidentRecoveryResult result, int alertCount) {
        return new AopExceptionRecoveryResponse(
                result.getIncidentId(),
                result.getOperatorId(),
                result.getStatus(),
                result.getExecutedSteps(),
                alertCount
        );
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getExecutedSteps() {
        return executedSteps;
    }

    public int getAlertCount() {
        return alertCount;
    }
}
```
</details>

### 4.8 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/dto/AopExceptionSummaryResponse.java`
- 역할: 개념 요약 API 응답 DTO입니다.
- 상세 설명: 설명 문장, 핵심 키워드, 학습 포인트를 분리해 담습니다. 태스크 제출 시 AOP 개념 설명도 함께 요구되는 경우 이 응답이 요약 자료 역할을 합니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto;

import java.util.List;

public class AopExceptionSummaryResponse {

    private final String summary;
    private final List<String> keywords;
    private final List<String> notes;

    public AopExceptionSummaryResponse(String summary, List<String> keywords, List<String> notes) {
        this.summary = summary;
        this.keywords = List.copyOf(keywords);
        this.notes = List.copyOf(notes);
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public List<String> getNotes() {
        return notes;
    }
}
```
</details>

### 4.9 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/exception/IncidentRecoveryException.java`
- 역할: 장애 복구 시나리오의 실패를 표현합니다.
- 상세 설명: 런타임 예외로 두어 AOP와 컨트롤러 예외 핸들러가 자연스럽게 후처리할 수 있게 했습니다. 메시지는 그대로 API 응답과 알림 이력에 반영됩니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.exception;

public class IncidentRecoveryException extends RuntimeException {

    public IncidentRecoveryException(String message) {
        super(message);
    }
}
```
</details>

### 4.10 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/service/AopExceptionHandlingService.java`
- 역할: 복구 작업을 수행하거나 실패를 재현하는 핵심 서비스입니다.
- 상세 설명: 공개 메서드 `recoverIncident()`가 태스크의 핵심 진입점입니다. `@NotifyOnException`이 붙어 있어 실패 시 Aspect가 개입합니다. 입력 검증은 `IllegalArgumentException`으로 처리하고, `triggerFailure=true`일 때는 `IncidentRecoveryException`을 던집니다. `healthCheck()`는 AOP 비적용 비교용 메서드입니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.annotation.NotifyOnException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.exception.IncidentRecoveryException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AopExceptionHandlingService {

    @NotifyOnException(
            value = "incident-recovery",
            alertTarget = "slack://ops-critical-alert"
    )
    public IncidentRecoveryResult recoverIncident(String incidentId, String operatorId, boolean triggerFailure) {
        validateIncidentId(incidentId);
        validateOperatorId(operatorId);

        if (triggerFailure) {
            throw new IncidentRecoveryException(
                    incidentId + " 장애 복구 승인 중 외부 알림 연동이 실패했습니다."
            );
        }

        return new IncidentRecoveryResult(
                incidentId,
                operatorId,
                "RECOVERY_COMPLETED",
                List.of(
                        "장애 원인 분석 리포트를 조회했습니다.",
                        "복구 스크립트 실행 조건을 점검했습니다.",
                        "운영자 승인 후 복구 작업을 완료했습니다."
                )
        );
    }

    public String healthCheck() {
        return "task08-aop-exception-handling-ok";
    }

    private void validateIncidentId(String incidentId) {
        if (incidentId == null || incidentId.isBlank()) {
            throw new IllegalArgumentException("incidentId는 비어 있을 수 없습니다.");
        }
    }

    private void validateOperatorId(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            throw new IllegalArgumentException("operatorId는 비어 있을 수 없습니다.");
        }
    }
}
```
</details>

### 4.11 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/service/IncidentRecoveryResult.java`
- 역할: 서비스 내부에서 사용하는 복구 결과 모델입니다.
- 상세 설명: API 응답에 필요한 기본 데이터인 `incidentId`, `operatorId`, `status`, `executedSteps`를 담습니다. DTO에서 이 값을 읽어 응답으로 변환합니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service;

import java.util.List;

public class IncidentRecoveryResult {

    private final String incidentId;
    private final String operatorId;
    private final String status;
    private final List<String> executedSteps;

    public IncidentRecoveryResult(
            String incidentId,
            String operatorId,
            String status,
            List<String> executedSteps
    ) {
        this.incidentId = incidentId;
        this.operatorId = operatorId;
        this.status = status;
        this.executedSteps = List.copyOf(executedSteps);
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getExecutedSteps() {
        return executedSteps;
    }
}
```
</details>

### 4.12 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/support/ExceptionAlertEntry.java`
- 역할: 예외 알림 한 건의 세부 정보를 표현합니다.
- 상세 설명: phase, operation, method, alertTarget, exceptionType, message를 보관합니다. API 응답에서도 그대로 직렬화되므로, AOP가 어떤 예외를 어떤 메서드에서 감지했는지 바로 확인할 수 있습니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support;

public class ExceptionAlertEntry {

    private final String phase;
    private final String operation;
    private final String method;
    private final String alertTarget;
    private final String exceptionType;
    private final String message;

    public ExceptionAlertEntry(
            String phase,
            String operation,
            String method,
            String alertTarget,
            String exceptionType,
            String message
    ) {
        this.phase = phase;
        this.operation = operation;
        this.method = method;
        this.alertTarget = alertTarget;
        this.exceptionType = exceptionType;
        this.message = message;
    }

    public String getPhase() {
        return phase;
    }

    public String getOperation() {
        return operation;
    }

    public String getMethod() {
        return method;
    }

    public String getAlertTarget() {
        return alertTarget;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getMessage() {
        return message;
    }
}
```
</details>

### 4.13 `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/support/ExceptionAlertStore.java`
- 역할: 알림 이력을 메모리에 보관합니다.
- 상세 설명: `reset()`, `add()`, `getEntries()`, `getLatestEntry()`를 제공합니다. 테스트 시작 전 상태를 비우고, 예외 핸들러와 컨트롤러가 동일한 저장소를 읽을 수 있도록 `@Component` 빈으로 등록했습니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ExceptionAlertStore {

    private final List<ExceptionAlertEntry> entries = new ArrayList<>();

    public synchronized void reset() {
        entries.clear();
    }

    public synchronized void add(ExceptionAlertEntry entry) {
        entries.add(entry);
    }

    public synchronized List<ExceptionAlertEntry> getEntries() {
        return List.copyOf(entries);
    }

    public synchronized Optional<ExceptionAlertEntry> getLatestEntry() {
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entries.get(entries.size() - 1));
    }
}
```
</details>

### 4.14 `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task08_aop_exception_handling/AopExceptionHandlingControllerTest.java`
- 역할: 태스크 8의 동작을 통합 검증합니다.
- 상세 설명: `exceptionHandledService_isProxiedBySpringAop()`는 프록시 적용 여부를 보장합니다. `recoverIncident_whenFailure_thenAspectLogsAndStoresAlert()`는 로그와 알림 저장을 검증합니다. 성공/실패 API 테스트는 정상 응답과 예외 응답, 알림 조회 API, 비적용 메서드까지 함께 확인합니다.

<details>
<summary>전체 코드 보기</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.aspect.ExceptionAlertAspect;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.exception.IncidentRecoveryException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service.AopExceptionHandlingService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AopExceptionHandlingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AopExceptionHandlingService aopExceptionHandlingService;

    @Autowired
    private ExceptionAlertStore exceptionAlertStore;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        exceptionAlertStore.reset();
    }

    @Test
    void exceptionHandledService_isProxiedBySpringAop() {
        assertThat(AopUtils.isAopProxy(aopExceptionHandlingService)).isTrue();
    }

    @Test
    void recoverIncident_whenFailure_thenAspectLogsAndStoresAlert() {
        Logger logger = (Logger) LoggerFactory.getLogger(ExceptionAlertAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            assertThatThrownBy(() -> aopExceptionHandlingService.recoverIncident("incident-500", "ops-bot", true))
                    .isInstanceOf(IncidentRecoveryException.class)
                    .hasMessage("incident-500 장애 복구 승인 중 외부 알림 연동이 실패했습니다.");

            assertThat(listAppender.list)
                    .hasSize(1)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains("[TASK08-AOP][ALERT][incident-recovery]")
                            && message.contains("AopExceptionHandlingService.recoverIncident")
                            && message.contains("target=slack://ops-critical-alert")
                            && message.contains("IncidentRecoveryException"));

            assertThat(exceptionAlertStore.getEntries())
                    .hasSize(1)
                    .extracting("phase", "operation", "method", "alertTarget", "exceptionType")
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple(
                                    "AFTER_THROWING",
                                    "incident-recovery",
                                    "AopExceptionHandlingService.recoverIncident",
                                    "slack://ops-critical-alert",
                                    "IncidentRecoveryException"
                            )
                    );
        } finally {
            logger.detachAppender(listAppender);
        }
    }

    @Test
    void summary_returnsAopExceptionKeywords() throws Exception {
        mockMvc.perform(get("/mission06/task08/aop-exception-handling/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keywords", hasSize(5)))
                .andExpect(jsonPath("$.keywords[3]").value("AfterThrowing"))
                .andExpect(jsonPath("$.notes[1]").value("비즈니스 서비스는 예외를 던지는 책임에 집중하고, 공통 대응은 Aspect가 맡습니다."));
    }

    @Test
    void recoverIncident_whenSuccess_thenReturnsResultWithoutAlerts() throws Exception {
        mockMvc.perform(get("/mission06/task08/aop-exception-handling/incidents/incident-101/recovery")
                        .param("operatorId", "ops-engineer")
                        .param("triggerFailure", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentId").value("incident-101"))
                .andExpect(jsonPath("$.operatorId").value("ops-engineer"))
                .andExpect(jsonPath("$.status").value("RECOVERY_COMPLETED"))
                .andExpect(jsonPath("$.alertCount").value(0))
                .andExpect(jsonPath("$.executedSteps[2]").value("운영자 승인 후 복구 작업을 완료했습니다."));

        mockMvc.perform(get("/mission06/task08/aop-exception-handling/alerts/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void recoverIncident_whenFailure_thenReturnsErrorResponseAndLatestAlert() throws Exception {
        mockMvc.perform(get("/mission06/task08/aop-exception-handling/incidents/incident-900/recovery")
                        .param("operatorId", "ops-engineer")
                        .param("triggerFailure", "true"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("incident-900 장애 복구 승인 중 외부 알림 연동이 실패했습니다."))
                .andExpect(jsonPath("$.alertCount").value(1))
                .andExpect(jsonPath("$.latestAlert.phase").value("AFTER_THROWING"))
                .andExpect(jsonPath("$.latestAlert.operation").value("incident-recovery"))
                .andExpect(jsonPath("$.latestAlert.alertTarget").value("slack://ops-critical-alert"))
                .andExpect(jsonPath("$.latestAlert.exceptionType").value("IncidentRecoveryException"));

        mockMvc.perform(get("/mission06/task08/aop-exception-handling/alerts/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.entries[0].method").value("AopExceptionHandlingService.recoverIncident"))
                .andExpect(jsonPath("$.entries[0].message").value("incident-900 장애 복구 승인 중 외부 알림 연동이 실패했습니다."));
    }

    @Test
    void nonAnnotatedMethod_doesNotTriggerAlert() {
        aopExceptionHandlingService.healthCheck();

        assertThat(exceptionAlertStore.getEntries()).isEmpty();
    }
}
```
</details>

## 5. 새로 나온 개념 정리 + 참고 링크
### 5.1 `@AfterThrowing`
- 핵심: 대상 메서드가 예외를 던지고 종료될 때만 실행되는 advice입니다.
- 왜 쓰는가: 정상 흐름과 실패 흐름을 분리해, 실패 시 로깅/알림/감사 기록 같은 공통 후처리를 서비스 코드 밖으로 뺄 수 있습니다.
- 참고 링크
  - Spring Framework Reference - Declaring Advice: https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/advice.html

### 5.2 AOP 프록시
- 핵심: 스프링 AOP는 대상 객체를 직접 바꾸는 대신 프록시 객체를 만들어 메서드 호출 전후에 advice를 끼워 넣습니다.
- 왜 쓰는가: 비즈니스 클래스를 크게 건드리지 않고도 트랜잭션, 보안, 로깅, 예외 알림 같은 횡단 관심사를 붙일 수 있습니다.
- 참고 링크
  - Spring Framework Reference - Proxying Mechanisms: https://docs.spring.io/spring-framework/reference/core/aop/proxying.html

### 5.3 AOP 핵심 용어
- 핵심: Aspect, Join Point, Pointcut, Advice는 스프링 AOP를 읽을 때 가장 자주 만나는 기본 용어입니다.
- 왜 쓰는가: 어떤 시점에 어떤 공통 로직이 어떻게 묶여 실행되는지 설명할 때 이 용어들이 바로 설계 언어가 됩니다.
- 참고 링크
  - Spring Framework Reference - AOP Concepts: https://docs.spring.io/spring-framework/reference/core/aop/introduction-defn.html
  - Spring Framework Reference - Aspect Oriented Programming with Spring: https://docs.spring.io/spring-framework/reference/core/aop.html

## 6. 실행·검증 방법
### 6.1 애플리케이션 실행
```bash
./gradlew bootRun
```

### 6.2 개념 요약 API 확인
```bash
curl -i http://localhost:8080/mission06/task08/aop-exception-handling/summary
```

예상 결과:
- HTTP 200
- `keywords`에 `AfterThrowing` 포함
- `notes`에 예외 후처리 분리 설명 포함

### 6.3 정상 복구 시나리오 확인
```bash
curl -i "http://localhost:8080/mission06/task08/aop-exception-handling/incidents/incident-101/recovery?operatorId=ops-engineer&triggerFailure=false"
```

예상 결과:
- HTTP 200
- `status=RECOVERY_COMPLETED`
- `alertCount=0`

### 6.4 실패 복구 시나리오 확인
```bash
curl -i "http://localhost:8080/mission06/task08/aop-exception-handling/incidents/incident-900/recovery?operatorId=ops-engineer&triggerFailure=true"
```

예상 결과:
- HTTP 500
- `message`에 장애 복구 승인 실패 문구 포함
- `latestAlert.operation=incident-recovery`
- `latestAlert.exceptionType=IncidentRecoveryException`

### 6.5 알림 이력 조회
```bash
curl -i http://localhost:8080/mission06/task08/aop-exception-handling/alerts/latest
```

예상 결과:
- 실패 시나리오를 한 번 실행한 뒤 `count=1`
- `entries[0].method=AopExceptionHandlingService.recoverIncident`

### 6.6 테스트 실행
```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.AopExceptionHandlingControllerTest
```

예상 결과:
- `BUILD SUCCESSFUL`
- 프록시 적용, 예외 로그, 알림 저장, 성공/실패 API 응답 검증 통과

## 7. 결과 확인 방법
- 성공 기준은 세 가지입니다. 정상 호출에서는 `alertCount=0`이어야 하고, 실패 호출에서는 HTTP 500과 함께 `latestAlert`가 채워져야 하며, 알림 조회 API에서는 저장된 예외 이력이 보여야 합니다.
- 실제 HTTP 응답 전문은 아래 파일에 저장했습니다.
  - `docs/mission-06-spring-core-advanced/task-08-aop-exception-handling/responses/summary-response.txt`
  - `docs/mission-06-spring-core-advanced/task-08-aop-exception-handling/responses/recovery-success-response.txt`
  - `docs/mission-06-spring-core-advanced/task-08-aop-exception-handling/responses/recovery-failure-response.txt`
  - `docs/mission-06-spring-core-advanced/task-08-aop-exception-handling/responses/alerts-response.txt`
- 테스트 실행 결과 요약은 아래 파일에 저장했습니다.
  - `docs/mission-06-spring-core-advanced/task-08-aop-exception-handling/task08-gradle-test-output.txt`

## 8. 학습 내용
이번 구현에서 중요한 점은 “예외를 어디서 처리할 것인가”를 분리한 것입니다. 서비스는 실패를 감추지 않고 명확한 예외를 던지고, Aspect는 그 실패 사실을 공통 형식으로 기록합니다. 덕분에 서비스 메서드마다 같은 `try-catch + log + alert` 코드를 반복하지 않아도 됩니다.

또 하나 확인한 점은 스프링 AOP가 프록시 기반이라는 사실입니다. 테스트에서 서비스 빈이 프록시인지 먼저 검증한 이유도 여기에 있습니다. AOP가 붙는 조건, 붙지 않는 조건을 이해하고 써야 예외 처리 로직이 예상대로 동작합니다. 이번 태스크는 그 흐름을 `@NotifyOnException`과 `@AfterThrowing` 조합으로 비교적 단순하게 확인할 수 있는 예제입니다.
