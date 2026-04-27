# 스프링 핵심 원리 - 고급: @Aspect 어노테이션을 활용한 로깅 시스템 구현

이 문서는 `mission-06-spring-core-advanced`의 `task-05-aspect-logging` 결과를 정리한 보고서입니다.
스프링 AOP의 `@Aspect`와 `@Around`를 사용해 특정 서비스 메서드 실행 전후에 로그를 남기고, 실행 결과와 실행 시간까지 함께 기록하는 구조를 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-06-spring-core-advanced` / `task-05-aspect-logging`
- 목표:
  - `@Aspect` 기반 애스펙트를 만들어 특정 메서드 호출 전후를 가로챕니다.
  - 시작 로그에는 메서드명과 인자, 종료 로그에는 결과값과 실행 시간(ms)을 기록합니다.
  - 애너테이션이 붙은 메서드만 선택적으로 추적하고, 비대상 메서드는 로그를 남기지 않도록 분리합니다.
  - 테스트에서 AOP 프록시 적용 여부, 전후 로그 기록, API 응답과 로그 조회를 함께 검증합니다.
- 엔드포인트:
  - `GET /mission06/task05/aspect-logging/reports/{reportId}`
  - `GET /mission06/task05/aspect-logging/logs/latest`
  - `GET /mission06/task05/aspect-logging/health`

설계한 시스템 정의:

- 포인트컷 기준 애너테이션: `@LoggableOperation`
- 애스펙트: `AspectMethodLoggingAspect`
- 대상 서비스: `AspectLoggingReportService`
- 실행 결과 모델: `ReportGenerationResult`
- 로그 저장소: `AspectLogStore`
- 로그 조회 응답: `AspectLogHistoryResponse`

핵심 동작 규칙:

1. `@LoggableOperation`이 붙은 메서드만 AOP 프록시가 가로챕니다.
2. 메서드 진입 시 `START` 로그에 메서드명과 인자를 기록합니다.
3. 메서드 정상 종료 시 `END` 로그에 반환 결과와 실행 시간(ms)을 기록합니다.
4. 같은 로그 내용을 `AspectLogStore`에도 저장해 API와 테스트에서 재확인할 수 있게 합니다.

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Annotation | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/annotation/LoggableOperation.java` | AOP 적용 대상 메서드를 표시하는 커스텀 애너테이션 |
| Aspect | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/aspect/AspectMethodLoggingAspect.java` | 메서드 실행 전후 로그와 실행 시간 기록 담당 |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/controller/AspectLoggingController.java` | 보고서 생성 API, 로그 조회 API, 헬스 체크 API 제공 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/dto/ReportGenerationResponse.java` | 보고서 생성 결과를 API 응답 형태로 변환 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/dto/AspectLogHistoryResponse.java` | 최근 AOP 로그 목록을 API 응답으로 반환 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/service/AspectLoggingReportService.java` | 로깅 대상 비즈니스 메서드와 비교용 비대상 메서드 제공 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/service/ReportGenerationResult.java` | 서비스 내부 실행 결과 모델 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/support/AspectLogEntry.java` | 로그 한 건의 phase, 메서드명, 상세 메시지, 실행 시간을 담는 모델 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/support/AspectLogStore.java` | 최근 추적 결과를 메모리에 저장하는 로그 저장소 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/AspectLoggingControllerTest.java` | 프록시 생성, 로그 기록, 비대상 메서드 제외, API 응답을 통합 검증 |

## 3. 구현 단계와 주요 코드 해설

1. `@LoggableOperation` 애너테이션을 만들어 “어떤 메서드를 추적할지”를 코드에 직접 표시했습니다.
   - 패키지 전체를 포인트컷으로 넓게 잡지 않고, 필요한 메서드만 골라 추적할 수 있습니다.
   - 나중에 로깅 대상이 늘어나더라도 애너테이션만 붙이면 같은 애스펙트를 재사용할 수 있습니다.

2. `AspectMethodLoggingAspect`에서 `@Around("@annotation(loggableOperation)")` 어드바이스로 메서드 전후를 감쌌습니다.
   - 시작 시점에는 메서드 라벨과 인자 목록을 기록합니다.
   - 종료 시점에는 `String.valueOf(result)`로 결과 객체 요약을 남기고, `System.nanoTime()` 차이값을 ms로 변환해 실행 시간을 함께 출력합니다.
   - 예외가 발생하는 경우에도 `ERROR` phase와 실행 시간을 저장하도록 분기해 두었습니다.

3. 서비스 계층은 AOP 데모가 잘 보이도록 “대상 메서드”와 “비대상 메서드”를 분리했습니다.
   - `generateReport()`는 `@LoggableOperation("report-generation")`이 붙어 있어 추적 대상입니다.
   - `healthCheck()`는 애너테이션이 없기 때문에 AOP 로그가 생기지 않습니다.
   - 이 차이를 테스트에서 바로 확인할 수 있어 “특정 메서드만 선택적으로 적용된다”는 점이 분명해집니다.

4. `AspectLogStore`와 로그 조회 API를 추가해 콘솔 외부에서도 AOP 동작 결과를 확인할 수 있게 했습니다.
   - `START` / `END` 로그를 메모리에 저장해 `GET /logs/latest`에서 다시 확인합니다.
   - 문서 제출용 응답 스냅샷 파일도 이 API 구조를 기준으로 남겼습니다.

5. 테스트는 단순 호출 확인이 아니라 AOP 핵심 요구사항을 각각 고정했습니다.
   - 서비스 빈이 실제로 AOP 프록시인지 검증
   - 시작/종료 로그에 메서드명, 결과값, 실행 시간이 포함되는지 검증
   - 애너테이션 없는 메서드는 로그가 생기지 않는지 검증
   - 실제 API 응답과 로그 조회 응답까지 MockMvc로 검증

요청 흐름 요약:

1. `AspectLoggingController`가 보고서 생성 요청을 받습니다.
2. 컨트롤러는 `AspectLoggingReportService.generateReport()`를 호출합니다.
3. 스프링 AOP 프록시가 호출을 가로채 `AspectMethodLoggingAspect`를 먼저 실행합니다.
4. 애스펙트는 `START` 로그를 남긴 뒤 실제 서비스 메서드를 실행합니다.
5. 서비스가 `ReportGenerationResult`를 반환하면 애스펙트가 `END` 로그와 실행 시간을 기록합니다.
6. 같은 로그는 `AspectLogStore`에 저장되고, `GET /logs/latest`로 다시 확인할 수 있습니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `LoggableOperation.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/annotation/LoggableOperation.java`
- 역할: AOP 적용 대상 메서드를 표시하는 커스텀 애너테이션
- 상세 설명:
- 메서드 레벨에 붙여 어떤 메서드를 애스펙트가 추적해야 하는지 명시합니다.
- `value()`에는 사람이 읽기 쉬운 작업 이름을 넣어 로그 메시지에 재사용합니다.
- 실행 시점까지 애너테이션 정보를 읽어야 하므로 `RetentionPolicy.RUNTIME`을 사용합니다.

<details>
<summary><code>LoggableOperation.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggableOperation {

    String value() default "";
}
```

</details>

### 4.2 `AspectMethodLoggingAspect.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/aspect/AspectMethodLoggingAspect.java`
- 역할: 메서드 실행 전후 로그와 실행 시간 기록 담당
- 상세 설명:
- `@Around("@annotation(loggableOperation)")`로 `@LoggableOperation`이 붙은 메서드만 추적합니다.
- 진입 시 `START`, 정상 종료 시 `END`, 예외 발생 시 `ERROR` phase를 기록합니다.
- `AspectLogStore`에도 동일한 내용을 저장해 테스트와 로그 조회 API가 같은 데이터를 확인하도록 맞췄습니다.

<details>
<summary><code>AspectMethodLoggingAspect.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.aspect;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.annotation.LoggableOperation;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogEntry;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogStore;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectMethodLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AspectMethodLoggingAspect.class);

    private final AspectLogStore aspectLogStore;

    public AspectMethodLoggingAspect(AspectLogStore aspectLogStore) {
        this.aspectLogStore = aspectLogStore;
    }

    @Around("@annotation(loggableOperation)")
    public Object logExecution(ProceedingJoinPoint joinPoint, LoggableOperation loggableOperation) throws Throwable {
        String methodLabel = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "."
                + joinPoint.getSignature().getName();
        String operation = resolveOperationName(loggableOperation, methodLabel);
        String argumentSummary = Arrays.deepToString(joinPoint.getArgs());

        aspectLogStore.reset();
        aspectLogStore.add(new AspectLogEntry("START", operation, methodLabel, "args=" + argumentSummary, null));
        log.info("[TASK05-AOP][START][{}] {} args={}", operation, methodLabel, argumentSummary);

        long start = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            String resultSummary = String.valueOf(result);

            aspectLogStore.add(new AspectLogEntry("END", operation, methodLabel, "result=" + resultSummary, elapsedMs));
            log.info("[TASK05-AOP][END][{}] {} result={} elapsedMs={}", operation, methodLabel, resultSummary, elapsedMs);
            return result;
        } catch (Throwable throwable) {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            String errorSummary = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();

            aspectLogStore.add(new AspectLogEntry("ERROR", operation, methodLabel, "error=" + errorSummary, elapsedMs));
            log.info("[TASK05-AOP][ERROR][{}] {} error={} elapsedMs={}", operation, methodLabel, errorSummary, elapsedMs);
            throw throwable;
        }
    }

    private String resolveOperationName(LoggableOperation loggableOperation, String methodLabel) {
        if (loggableOperation.value() == null || loggableOperation.value().isBlank()) {
            return methodLabel;
        }
        return loggableOperation.value();
    }
}
```

</details>

### 4.3 `AspectLoggingController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/controller/AspectLoggingController.java`
- 역할: 보고서 생성 API, 로그 조회 API, 헬스 체크 API 제공
- 상세 설명:
- 기본 경로: `/mission06/task05/aspect-logging`
- 매핑 메서드:
  - `GET /reports/{reportId}` -> 200 OK, 보고서 생성 결과 JSON 반환
  - `GET /logs/latest` -> 200 OK, 최근 AOP 로그 목록 JSON 반환
  - `GET /health` -> 200 OK, 일반 문자열 반환
- 컨트롤러는 서비스 실행 결과와 저장소 내용을 응답 형태로 변환하는 역할만 담당합니다.

<details>
<summary><code>AspectLoggingController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.dto.AspectLogHistoryResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.dto.ReportGenerationResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.service.AspectLoggingReportService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task05/aspect-logging")
public class AspectLoggingController {

    private final AspectLoggingReportService aspectLoggingReportService;
    private final AspectLogStore aspectLogStore;

    public AspectLoggingController(
            AspectLoggingReportService aspectLoggingReportService,
            AspectLogStore aspectLogStore
    ) {
        this.aspectLoggingReportService = aspectLoggingReportService;
        this.aspectLogStore = aspectLogStore;
    }

    @GetMapping("/reports/{reportId}")
    public ReportGenerationResponse generateReport(
            @PathVariable String reportId,
            @RequestParam(defaultValue = "ops-team") String operator,
            @RequestParam(defaultValue = "true") boolean includeDraftSection
    ) {
        return ReportGenerationResponse.from(
                aspectLoggingReportService.generateReport(reportId, operator, includeDraftSection)
        );
    }

    @GetMapping("/logs/latest")
    public AspectLogHistoryResponse latestLogs() {
        return new AspectLogHistoryResponse(aspectLogStore.getEntries());
    }

    @GetMapping("/health")
    public String health() {
        return aspectLoggingReportService.healthCheck();
    }
}
```

</details>

### 4.4 `ReportGenerationResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/dto/ReportGenerationResponse.java`
- 역할: 보고서 생성 결과를 API 응답 형태로 변환
- 상세 설명:
- 서비스 내부 모델 `ReportGenerationResult`를 컨트롤러 응답 전용 구조로 바꿉니다.
- 응답에는 보고서 ID, 작업자, 생성 섹션 목록, 결과 메시지를 포함합니다.
- `from()` 정적 팩토리 메서드를 써서 컨트롤러가 변환 책임을 쉽게 사용할 수 있게 했습니다.

<details>
<summary><code>ReportGenerationResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.service.ReportGenerationResult;
import java.util.List;

public class ReportGenerationResponse {

    private final String reportId;
    private final String operator;
    private final boolean includeDraftSection;
    private final String status;
    private final String resultMessage;
    private final String digest;
    private final List<String> generatedSections;

    public ReportGenerationResponse(
            String reportId,
            String operator,
            boolean includeDraftSection,
            String status,
            String resultMessage,
            String digest,
            List<String> generatedSections
    ) {
        this.reportId = reportId;
        this.operator = operator;
        this.includeDraftSection = includeDraftSection;
        this.status = status;
        this.resultMessage = resultMessage;
        this.digest = digest;
        this.generatedSections = List.copyOf(generatedSections);
    }

    public static ReportGenerationResponse from(ReportGenerationResult result) {
        return new ReportGenerationResponse(
                result.getReportId(),
                result.getOperator(),
                result.isIncludeDraftSection(),
                result.getStatus(),
                result.getResultMessage(),
                result.getDigest(),
                result.getGeneratedSections()
        );
    }

    public String getReportId() {
        return reportId;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isIncludeDraftSection() {
        return includeDraftSection;
    }

    public String getStatus() {
        return status;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getDigest() {
        return digest;
    }

    public List<String> getGeneratedSections() {
        return generatedSections;
    }
}
```

</details>

### 4.5 `AspectLogHistoryResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/dto/AspectLogHistoryResponse.java`
- 역할: 최근 AOP 로그 목록을 API 응답으로 반환
- 상세 설명:
- `AspectLogStore`에 저장된 최근 로그 목록을 그대로 감싸 응답합니다.
- `count` 필드를 별도로 두어 로그 개수가 예상대로 2건(`START`, `END`)인지 빠르게 확인할 수 있습니다.
- 로그 엔트리는 `AspectLogEntry`의 getter를 통해 JSON으로 직렬화됩니다.

<details>
<summary><code>AspectLogHistoryResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogEntry;
import java.util.List;

public class AspectLogHistoryResponse {

    private final int count;
    private final List<AspectLogEntry> entries;

    public AspectLogHistoryResponse(List<AspectLogEntry> entries) {
        this.count = entries.size();
        this.entries = List.copyOf(entries);
    }

    public int getCount() {
        return count;
    }

    public List<AspectLogEntry> getEntries() {
        return entries;
    }
}
```

</details>

### 4.6 `AspectLoggingReportService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/service/AspectLoggingReportService.java`
- 역할: 로깅 대상 비즈니스 메서드와 비교용 비대상 메서드 제공
- 상세 설명:
- 핵심 공개 메서드:
  - `generateReport()` -> 보고서 생성 결과 반환, `@LoggableOperation`으로 추적 대상
  - `healthCheck()` -> 단순 상태 확인 문자열 반환, 추적 대상 아님
- 트랜잭션은 사용하지 않았고, 이 서비스는 “반환 결과가 있는 메서드를 AOP가 어떻게 감싸는가”를 보여주는 데 집중합니다.
- 입력값 검증은 `validate()`로 분리해 빈 문자열이 들어오면 즉시 예외를 발생시킵니다.

<details>
<summary><code>AspectLoggingReportService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.annotation.LoggableOperation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AspectLoggingReportService {

    @LoggableOperation("report-generation")
    public ReportGenerationResult generateReport(String reportId, String operator, boolean includeDraftSection) {
        validate(reportId, operator);

        List<String> generatedSections = new ArrayList<>();
        generatedSections.add("overview");
        generatedSections.add("aop-log-summary");
        generatedSections.add("timing-analysis");

        if (includeDraftSection) {
            generatedSections.add("draft-appendix");
        }

        String digest = reportId + "|" + operator + "|" + generatedSections.size();
        String resultMessage = operator + " 사용자가 " + reportId + " 보고서를 생성했습니다.";

        return new ReportGenerationResult(
                reportId,
                operator,
                includeDraftSection,
                "COMPLETED",
                resultMessage,
                digest,
                generatedSections
        );
    }

    public String healthCheck() {
        return "aspect-logging-ready";
    }

    private void validate(String reportId, String operator) {
        if (reportId == null || reportId.isBlank()) {
            throw new IllegalArgumentException("reportId는 비어 있을 수 없습니다.");
        }
        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("operator는 비어 있을 수 없습니다.");
        }
    }
}
```

</details>

### 4.7 `ReportGenerationResult.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/service/ReportGenerationResult.java`
- 역할: 서비스 내부 실행 결과 모델
- 상세 설명:
- 서비스 메서드가 만든 결과 데이터를 한 객체로 묶습니다.
- `toString()`을 직접 구현해 애스펙트 로그에 객체 해시값 대신 읽을 수 있는 결과 요약이 남도록 했습니다.
- 이 덕분에 테스트에서 결과 로그에 실제 보고서 ID와 상태가 포함되는지 확인할 수 있습니다.

<details>
<summary><code>ReportGenerationResult.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.service;

import java.util.List;

public class ReportGenerationResult {

    private final String reportId;
    private final String operator;
    private final boolean includeDraftSection;
    private final String status;
    private final String resultMessage;
    private final String digest;
    private final List<String> generatedSections;

    public ReportGenerationResult(
            String reportId,
            String operator,
            boolean includeDraftSection,
            String status,
            String resultMessage,
            String digest,
            List<String> generatedSections
    ) {
        this.reportId = reportId;
        this.operator = operator;
        this.includeDraftSection = includeDraftSection;
        this.status = status;
        this.resultMessage = resultMessage;
        this.digest = digest;
        this.generatedSections = List.copyOf(generatedSections);
    }

    public String getReportId() {
        return reportId;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isIncludeDraftSection() {
        return includeDraftSection;
    }

    public String getStatus() {
        return status;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getDigest() {
        return digest;
    }

    public List<String> getGeneratedSections() {
        return generatedSections;
    }

    @Override
    public String toString() {
        return "ReportGenerationResult{"
                + "reportId='" + reportId + '\''
                + ", operator='" + operator + '\''
                + ", includeDraftSection=" + includeDraftSection
                + ", status='" + status + '\''
                + ", digest='" + digest + '\''
                + ", generatedSections=" + generatedSections
                + '}';
    }
}
```

</details>

### 4.8 `AspectLogEntry.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/support/AspectLogEntry.java`
- 역할: 로그 한 건의 phase, 메서드명, 상세 메시지, 실행 시간을 담는 모델
- 상세 설명:
- `START`, `END`, `ERROR` 중 어떤 단계에서 기록된 로그인지 `phase`로 구분합니다.
- `detail`에는 인자 목록, 결과값, 예외 메시지 같은 실제 로그 내용을 저장합니다.
- `elapsedMs`는 시작 로그에서는 `null`, 종료/오류 로그에서는 측정값을 담도록 설계했습니다.

<details>
<summary><code>AspectLogEntry.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support;

public class AspectLogEntry {

    private final String phase;
    private final String operation;
    private final String method;
    private final String detail;
    private final Long elapsedMs;

    public AspectLogEntry(String phase, String operation, String method, String detail, Long elapsedMs) {
        this.phase = phase;
        this.operation = operation;
        this.method = method;
        this.detail = detail;
        this.elapsedMs = elapsedMs;
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

    public String getDetail() {
        return detail;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }
}
```

</details>

### 4.9 `AspectLogStore.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/support/AspectLogStore.java`
- 역할: 최근 추적 결과를 메모리에 저장하는 로그 저장소
- 상세 설명:
- 최근 호출 로그를 메모리에 보관하고, 새 추적이 시작되면 `reset()`으로 이전 결과를 비웁니다.
- `synchronized` 메서드로 읽기/쓰기 충돌을 단순하게 막았습니다.
- 서비스 코드를 건드리지 않고도 AOP 결과를 API와 테스트에서 재사용할 수 있게 해 줍니다.

<details>
<summary><code>AspectLogStore.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AspectLogStore {

    private final List<AspectLogEntry> entries = new ArrayList<>();

    public synchronized void reset() {
        entries.clear();
    }

    public synchronized void add(AspectLogEntry entry) {
        entries.add(entry);
    }

    public synchronized List<AspectLogEntry> getEntries() {
        return List.copyOf(entries);
    }
}
```

</details>

### 4.10 `AspectLoggingControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task05_aspect_logging/AspectLoggingControllerTest.java`
- 역할: 프록시 생성, 로그 기록, 비대상 메서드 제외, API 응답을 통합 검증
- 상세 설명:
- 검증 시나리오:
  - `trackedService_isProxiedBySpringAop()` -> 서비스 빈이 실제 AOP 프록시인지 보장
  - `generateReport_logsBeforeAndAfterWithResultAndElapsedTime()` -> 시작/종료 로그와 실행 시간 기록 보장
  - `nonAnnotatedMethod_doesNotCreateAspectLogs()` -> 애너테이션 없는 메서드는 추적되지 않음을 보장
  - `reportEndpoint_andLatestLogEndpoint_returnExecutionResultAndStoredLogs()` -> API 결과와 로그 조회 응답 구조 보장
- 정상 흐름과 비대상 흐름을 함께 고정해, 포인트컷 범위가 흐트러지는 회귀를 잡을 수 있습니다.

<details>
<summary><code>AspectLoggingControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.aspect.AspectMethodLoggingAspect;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.service.AspectLoggingReportService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogStore;
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
class AspectLoggingControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AspectLoggingReportService aspectLoggingReportService;

    @Autowired
    private AspectLogStore aspectLogStore;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        aspectLogStore.reset();
    }

    @Test
    void trackedService_isProxiedBySpringAop() {
        assertThat(AopUtils.isAopProxy(aspectLoggingReportService)).isTrue();
    }

    @Test
    void generateReport_logsBeforeAndAfterWithResultAndElapsedTime() {
        Logger logger = (Logger) LoggerFactory.getLogger(AspectMethodLoggingAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            aspectLoggingReportService.generateReport("report-2026-05", "audit-bot", true);

            assertThat(listAppender.list)
                    .hasSize(2)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains("[TASK05-AOP][START][report-generation]")
                            && message.contains("AspectLoggingReportService.generateReport")
                            && message.contains("args=[report-2026-05, audit-bot, true]"))
                    .anyMatch(message -> message.contains("[TASK05-AOP][END][report-generation]")
                            && message.contains("ReportGenerationResult{reportId='report-2026-05'")
                            && message.contains("elapsedMs="));

            assertThat(aspectLogStore.getEntries())
                    .hasSize(2)
                    .extracting("phase")
                    .containsExactly("START", "END");

            assertThat(aspectLogStore.getEntries().get(1).getDetail())
                    .contains("report-2026-05");
            assertThat(aspectLogStore.getEntries().get(1).getElapsedMs())
                    .isNotNull()
                    .isGreaterThanOrEqualTo(0L);
        } finally {
            logger.detachAppender(listAppender);
        }
    }

    @Test
    void nonAnnotatedMethod_doesNotCreateAspectLogs() {
        aspectLoggingReportService.healthCheck();

        assertThat(aspectLogStore.getEntries()).isEmpty();
    }

    @Test
    void reportEndpoint_andLatestLogEndpoint_returnExecutionResultAndStoredLogs() throws Exception {
        mockMvc.perform(get("/mission06/task05/aspect-logging/reports/report-2026-ops")
                        .param("operator", "ops-team")
                        .param("includeDraftSection", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value("report-2026-ops"))
                .andExpect(jsonPath("$.operator").value("ops-team"))
                .andExpect(jsonPath("$.includeDraftSection").value(false))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.generatedSections[0]").value("overview"))
                .andExpect(jsonPath("$.generatedSections[2]").value("timing-analysis"));

        mockMvc.perform(get("/mission06/task05/aspect-logging/logs/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.entries[0].phase").value("START"))
                .andExpect(jsonPath("$.entries[0].operation").value("report-generation"))
                .andExpect(jsonPath("$.entries[1].phase").value("END"))
                .andExpect(jsonPath("$.entries[1].method").value("AspectLoggingReportService.generateReport"))
                .andExpect(jsonPath("$.entries[1].detail").value(org.hamcrest.Matchers.containsString("report-2026-ops")))
                .andExpect(jsonPath("$.entries[1].elapsedMs").isNumber());
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- `@Aspect`
  - 핵심: 공통 관심사를 별도 클래스로 분리하고, 스프링이 프록시를 통해 대상 메서드 호출 전후에 끼워 넣게 만듭니다.
  - 왜 쓰는가: 로깅, 보안, 트랜잭션처럼 여러 서비스에 반복되는 코드를 메서드마다 직접 넣지 않아도 되기 때문입니다.
  - 참고 링크:
    - https://docs.spring.io/spring-framework/reference/core/aop.html
    - https://docs.spring.io/spring-framework/reference/core/aop/ataspectj.html

- `@Around`
  - 핵심: 대상 메서드 실행 전후를 한 메서드 안에서 감싸며, 실제 메서드 실행 여부도 직접 제어할 수 있습니다.
  - 왜 쓰는가: 시작 로그, 종료 로그, 실행 시간 측정처럼 “전후를 한 쌍으로 묶어 처리”해야 할 때 가장 자연스럽기 때문입니다.
  - 참고 링크:
    - https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/advice.html

- `ProceedingJoinPoint`
  - 핵심: 현재 가로챈 메서드의 시그니처, 인자, 실제 실행(`proceed()`) 기능을 제공하는 객체입니다.
  - 왜 쓰는가: 어떤 메서드를 호출했는지 읽고, 인자를 로그로 남기고, 실제 비즈니스 메서드를 실행시키려면 이 객체가 필요합니다.
  - 참고 링크:
    - https://www.eclipse.org/aspectj/doc/released/runtime-api/org/aspectj/lang/ProceedingJoinPoint.html

- 커스텀 애너테이션 기반 포인트컷
  - 핵심: 패키지 전체가 아니라 `@LoggableOperation`이 붙은 메서드만 선택적으로 AOP 적용 대상으로 만들 수 있습니다.
  - 왜 쓰는가: 필요 없는 메서드까지 모두 추적하지 않아도 되고, 로깅 범위를 코드에서 명확히 읽을 수 있기 때문입니다.
  - 참고 링크:
    - https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/advice.html#aop-ataspectj-advice-params

## 6. 실행·검증 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

보고서 생성 API 호출:

```bash
curl -s "http://localhost:8080/mission06/task05/aspect-logging/reports/report-2026-ops?operator=ops-team&includeDraftSection=false"
```

최근 로그 조회 API 호출:

```bash
curl -s "http://localhost:8080/mission06/task05/aspect-logging/logs/latest"
```

비대상 메서드 확인용 헬스 체크:

```bash
curl -s "http://localhost:8080/mission06/task05/aspect-logging/health"
```

task05 테스트 실행:

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.AspectLoggingControllerTest
```

예상 결과:

- `/reports/{reportId}`는 보고서 생성 결과 JSON을 반환합니다.
- `/logs/latest`는 `START`, `END` 두 건의 로그와 실행 시간을 반환합니다.
- `/health`는 문자열만 반환하고, AOP 로그 저장소에는 새 로그가 남지 않아야 합니다.
- 테스트는 프록시 생성 여부, 로그 기록, 비대상 메서드 제외, API 응답 구조를 모두 통과해야 합니다.

## 7. 결과 확인 방법

- 성공 기준:
  - `/reports/{reportId}` 응답에 `reportId`, `status`, `generatedSections`가 정확히 포함되어야 합니다.
  - `/logs/latest` 응답에 `count=2`, `entries[0].phase=START`, `entries[1].phase=END`가 확인되어야 합니다.
  - 종료 로그에는 `result=`와 `elapsedMs`가 포함되어야 합니다.
  - `health` 호출만 수행했을 때는 새 로그가 기록되지 않아야 합니다.

- 응답 스냅샷 파일:
  - `docs/mission-06-spring-core-advanced/task-05-aspect-logging/responses/report-generation-response.txt`
  - `docs/mission-06-spring-core-advanced/task-05-aspect-logging/responses/latest-log-history.txt`

- 테스트 로그 파일:
  - `docs/mission-06-spring-core-advanced/task-05-aspect-logging/task05-gradle-test-output.txt`

응답 스냅샷 예시:

```json
{
  "reportId": "report-2026-ops",
  "operator": "ops-team",
  "includeDraftSection": false,
  "status": "COMPLETED",
  "resultMessage": "ops-team 사용자가 report-2026-ops 보고서를 생성했습니다.",
  "digest": "report-2026-ops|ops-team|3",
  "generatedSections": [
    "overview",
    "aop-log-summary",
    "timing-analysis"
  ]
}
```

최근 로그 스냅샷에서는 `elapsedMs` 값이 실행 환경에 따라 달라질 수 있지만, `START -> END` 순서와 `result=` 포함 여부는 동일하게 유지되어야 합니다.

## 8. 학습 내용

- 스프링 AOP는 비즈니스 코드 안에 직접 로그 코드를 넣지 않아도, 프록시를 통해 실행 전후 동작을 공통으로 끼워 넣을 수 있습니다. 그래서 서비스 메서드는 본래 책임에 집중하고, 로깅 규칙은 애스펙트 한 곳에서 관리할 수 있습니다.
- `@Around`는 메서드 실행 전후를 모두 제어할 수 있어서 “시작 로그 + 종료 로그 + 실행 시간 측정”처럼 짝이 맞는 부가 기능에 적합합니다. `proceed()` 호출 전에는 인자를 보고, 호출 후에는 반환값을 확인할 수 있다는 점이 핵심입니다.
- 포인트컷을 애너테이션 기반으로 좁히면 어느 메서드가 추적 대상인지 코드만 봐도 분명합니다. 패키지 전체를 한꺼번에 묶는 방식보다 의도가 선명하고, 원하지 않는 메서드에 AOP가 붙는 실수를 줄일 수 있습니다.
- 결과 객체의 `toString()` 형태도 로그 품질에 직접 영향을 줍니다. 반환값 로그를 사람이 읽을 수 있게 남기려면, 객체 해시값 대신 핵심 필드가 드러나는 문자열 표현을 준비하는 편이 훨씬 유용합니다.
