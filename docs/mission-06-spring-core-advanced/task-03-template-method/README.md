# 스프링 핵심 원리 - 고급: 템플릿 메서드 패턴을 활용한 확장 가능한 시스템 설계

이 문서는 `mission-06-spring-core-advanced`의 `task-03-template-method`를 기준으로 정리한 보고서입니다.
템플릿 메서드 패턴을 사용해 작업 실행 순서를 상위 클래스에 고정하고, 개별 작업에서 달라지는 단계만 하위 클래스가 오버라이드하도록 설계했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-06-spring-core-advanced` / `task-03-template-method`
- 목표:
  - 작업 실행 알고리즘의 뼈대를 상위 클래스에 고정한다.
  - 하위 클래스는 검증, 준비, 실행, 후처리처럼 바뀌는 단계만 구현한다.
  - 새 작업을 추가할 때 공통 흐름 코드를 복붙하지 않고, 하위 클래스만 추가해 확장할 수 있는 구조를 만든다.
- 엔드포인트:
  - `GET /mission06/task03/template-method/jobs`
  - `POST /mission06/task03/template-method/jobs/{jobType}/run`

설계한 시스템 정의:

- 템플릿 메서드 상위 클래스: `AbstractOperationTemplate`
- 고정된 실행 순서:
  1. 공통 시작
  2. 공통 입력 검증
  3. 개별 검증
  4. 개별 준비
  5. 공통 본 실행 진입
  6. 개별 실행
  7. 개별 후처리
  8. 공통 종료
- 확장 대상 하위 클래스:
  - `CacheWarmupOperationTemplate`
  - `ReportPublishOperationTemplate`
- 작업 디스패처: `TemplateOperationDispatcher`
  - 등록된 템플릿 목록 중 `jobType`에 맞는 구현체를 찾아 실행

핵심 설계 규칙:

1. `execute()` 메서드는 `final`로 선언해 하위 클래스가 전체 알고리즘 구조를 바꾸지 못하게 합니다.
2. 바뀌는 단계는 `validateTarget()`, `prepare()`, `executeCore()`, `afterExecute()`로 분리합니다.
3. 새 작업을 추가하려면 `AbstractOperationTemplate`를 상속한 클래스만 추가하면 됩니다.
4. 컨트롤러는 구현체를 직접 몰라도 `TemplateOperationDispatcher`만 호출하면 됩니다.

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/controller/TemplateMethodController.java` | 작업 목록 조회와 템플릿 실행 API 제공 |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/controller/TemplateMethodExceptionHandler.java` | 잘못된 `jobType` 또는 입력 오류를 JSON으로 변환 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/dto/TemplateCatalogResponse.java` | 지원 작업 목록과 템플릿 정의 문구 반환 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/dto/TemplateJobResponse.java` | 실행 결과와 단계별 수행 로그 반환 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/dto/TemplateMethodErrorResponse.java` | 오류 응답 구조 정의 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/AbstractOperationTemplate.java` | 템플릿 메서드와 공통 실행 알고리즘 정의 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/CacheWarmupOperationTemplate.java` | 캐시 예열 작업의 개별 단계 구현 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/ReportPublishOperationTemplate.java` | 보고서 발행 작업의 개별 단계 구현 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/TemplateJobResult.java` | 템플릿 실행 내부 결과 모델 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/TemplateOperationDispatcher.java` | `jobType`에 맞는 템플릿 구현체 선택 및 실행 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/TemplateMethodControllerTest.java` | `final` 템플릿 메서드, 정상 실행, 예외 흐름 검증 |

## 3. 구현 단계와 주요 코드 해설

1. `AbstractOperationTemplate`에 템플릿 메서드 `execute()`를 정의했습니다.
   - `execute()`는 `final`이라 하위 클래스가 전체 실행 순서를 바꾸지 못합니다.
   - 공통 시작, 공통 검증, 공통 종료는 상위 클래스가 책임집니다.

2. 하위 클래스는 작업마다 다른 부분만 구현하게 만들었습니다.
   - `CacheWarmupOperationTemplate`: 캐시 영역 검증, 키 목록 수집, 프리로드 요청
   - `ReportPublishOperationTemplate`: 보고서 식별자 검증, 메타데이터 로딩, 발행 처리

3. `TemplateOperationDispatcher`가 등록된 템플릿 구현체를 모아서 실행합니다.
   - `List<AbstractOperationTemplate>`를 주입받아 `jobType` 기준 맵을 구성합니다.
   - 작업 추가 시 컨트롤러 수정 없이 새 하위 클래스만 등록하면 됩니다.

4. `TemplateMethodController`는 작업 목록 조회와 실행 API를 제공합니다.
   - `GET /jobs`로 현재 등록된 작업 타입을 확인할 수 있습니다.
   - `POST /jobs/{jobType}/run`으로 실제 템플릿 실행 결과를 확인할 수 있습니다.

5. 예외 처리는 `TemplateMethodExceptionHandler`에서 공통 처리했습니다.
   - 지원하지 않는 `jobType`
   - 잘못된 `target`
   - 비어 있는 입력값
   이런 케이스를 모두 400 응답으로 내려 일관성 있게 확인할 수 있게 했습니다.

요청 흐름 요약:

1. 컨트롤러가 `jobType`, `target`, `operator`를 받습니다.
2. `TemplateOperationDispatcher`가 `jobType`에 맞는 템플릿 구현체를 선택합니다.
3. 선택된 템플릿의 `execute()`가 공통 알고리즘을 순서대로 실행합니다.
4. 하위 클래스는 개별 검증/준비/실행/후처리만 수행합니다.
5. 최종 결과는 `TemplateJobResponse`로 변환되어 반환됩니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `TemplateMethodController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/controller/TemplateMethodController.java`
- 역할: 작업 목록 조회와 템플릿 실행 API 제공
- 상세 설명:
- 기본 경로: `/mission06/task03/template-method`
- 매핑 메서드:
  - `GET /jobs` -> 지원 작업 목록 조회
  - `POST /jobs/{jobType}/run` -> 템플릿 실행
- 컨트롤러는 작업 타입과 파라미터를 받아 디스패처에 위임하고, 실행 결과를 응답 DTO로 변환합니다.

<details>
<summary><code>TemplateMethodController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto.TemplateCatalogResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto.TemplateJobResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service.TemplateOperationDispatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task03/template-method")
public class TemplateMethodController {

    private final TemplateOperationDispatcher templateOperationDispatcher;

    public TemplateMethodController(TemplateOperationDispatcher templateOperationDispatcher) {
        this.templateOperationDispatcher = templateOperationDispatcher;
    }

    @GetMapping("/jobs")
    public TemplateCatalogResponse jobs() {
        return new TemplateCatalogResponse(
                "공통 시작 -> 공통 검증 -> 개별 검증 -> 개별 준비 -> 공통 본 실행 -> 개별 실행 -> 개별 후처리 -> 공통 종료",
                templateOperationDispatcher.supportedJobTypes()
        );
    }

    @PostMapping("/jobs/{jobType}/run")
    public TemplateJobResponse run(
            @PathVariable String jobType,
            @RequestParam String target,
            @RequestParam(defaultValue = "system-operator") String operator
    ) {
        return TemplateJobResponse.from(templateOperationDispatcher.execute(jobType, target, operator));
    }
}
```

</details>

### 4.2 `TemplateMethodExceptionHandler.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/controller/TemplateMethodExceptionHandler.java`
- 역할: 잘못된 `jobType` 또는 입력 오류를 JSON으로 변환
- 상세 설명:
- `IllegalArgumentException`을 HTTP 400으로 통일해 반환합니다.
- 메시지와 요청 경로를 함께 포함해 어떤 입력이 잘못됐는지 바로 확인할 수 있습니다.

<details>
<summary><code>TemplateMethodExceptionHandler.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto.TemplateMethodErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TemplateMethodController.class)
public class TemplateMethodExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public TemplateMethodErrorResponse handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new TemplateMethodErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
```

</details>

### 4.3 `TemplateCatalogResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/dto/TemplateCatalogResponse.java`
- 역할: 지원 작업 목록과 템플릿 정의 문구 반환
- 상세 설명:
- 현재 시스템이 어떤 작업 타입을 지원하는지와, 템플릿 메서드 실행 순서를 문자열로 함께 반환합니다.
- 컨트롤러의 작업 목록 조회 응답에서 사용됩니다.

<details>
<summary><code>TemplateCatalogResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto;

import java.util.List;

public class TemplateCatalogResponse {

    private final String templateMethodDefinition;
    private final List<String> supportedJobTypes;

    public TemplateCatalogResponse(String templateMethodDefinition, List<String> supportedJobTypes) {
        this.templateMethodDefinition = templateMethodDefinition;
        this.supportedJobTypes = List.copyOf(supportedJobTypes);
    }

    public String getTemplateMethodDefinition() {
        return templateMethodDefinition;
    }

    public List<String> getSupportedJobTypes() {
        return supportedJobTypes;
    }
}
```

</details>

### 4.4 `TemplateJobResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/dto/TemplateJobResponse.java`
- 역할: 실행 결과와 단계별 수행 로그 반환
- 상세 설명:
- 작업 타입, 작업명, 대상, 요청자, 상태, 결과 메시지, 실행 단계 목록을 함께 담습니다.
- `from()` 팩토리 메서드로 내부 결과 객체를 외부 응답 형태로 변환합니다.

<details>
<summary><code>TemplateJobResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service.TemplateJobResult;
import java.util.List;

public class TemplateJobResponse {

    private final String jobType;
    private final String jobName;
    private final String target;
    private final String operator;
    private final String status;
    private final String resultMessage;
    private final List<String> steps;

    public TemplateJobResponse(
            String jobType,
            String jobName,
            String target,
            String operator,
            String status,
            String resultMessage,
            List<String> steps
    ) {
        this.jobType = jobType;
        this.jobName = jobName;
        this.target = target;
        this.operator = operator;
        this.status = status;
        this.resultMessage = resultMessage;
        this.steps = List.copyOf(steps);
    }

    public static TemplateJobResponse from(TemplateJobResult result) {
        return new TemplateJobResponse(
                result.getJobType(),
                result.getJobName(),
                result.getTarget(),
                result.getOperator(),
                result.getStatus(),
                result.getResultMessage(),
                result.getSteps()
        );
    }

    public String getJobType() {
        return jobType;
    }

    public String getJobName() {
        return jobName;
    }

    public String getTarget() {
        return target;
    }

    public String getOperator() {
        return operator;
    }

    public String getStatus() {
        return status;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public List<String> getSteps() {
        return steps;
    }
}
```

</details>

### 4.5 `TemplateMethodErrorResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/dto/TemplateMethodErrorResponse.java`
- 역할: 오류 응답 구조 정의
- 상세 설명:
- 상태 코드, 오류 이름, 메시지, 요청 경로를 담는 단순 DTO입니다.
- 입력 오류와 지원하지 않는 작업 타입 응답을 일관된 JSON 구조로 유지합니다.

<details>
<summary><code>TemplateMethodErrorResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto;

public class TemplateMethodErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public TemplateMethodErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
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
}
```

</details>

### 4.6 `AbstractOperationTemplate.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/AbstractOperationTemplate.java`
- 역할: 템플릿 메서드와 공통 실행 알고리즘 정의
- 상세 설명:
- 핵심 공개 메서드: `public final TemplateJobResult execute(String target, String operator)`
- `execute()`가 공통 시작, 공통 검증, 공통 종료를 고정하고, 개별 단계는 추상 메서드로 하위 클래스에 위임합니다.
- `final`로 선언해 전체 알고리즘 구조를 하위 클래스가 변경하지 못하도록 막았습니다.

<details>
<summary><code>AbstractOperationTemplate.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOperationTemplate {

    public final TemplateJobResult execute(String target, String operator) {
        List<String> steps = new ArrayList<>();

        steps.add("1. 공통 시작 단계: jobType=%s, operator=%s".formatted(jobType(), operator));
        validateCommonInput(target, operator);
        steps.add("2. 공통 입력 검증 완료");

        validateTarget(target, steps);
        prepare(target, operator, steps);

        steps.add("5. 공통 본 실행 단계 진입");
        String resultMessage = executeCore(target, operator, steps);

        afterExecute(target, operator, steps);
        steps.add("8. 공통 종료 단계: 결과 응답 조합 완료");

        return new TemplateJobResult(
                jobType(),
                jobName(),
                target,
                operator,
                "SUCCESS",
                resultMessage,
                steps
        );
    }

    public abstract String jobType();

    public abstract String jobName();

    protected abstract void validateTarget(String target, List<String> steps);

    protected abstract void prepare(String target, String operator, List<String> steps);

    protected abstract String executeCore(String target, String operator, List<String> steps);

    protected void afterExecute(String target, String operator, List<String> steps) {
        steps.add("7. 공통 후처리 단계: 실행 로그 정리");
    }

    private void validateCommonInput(String target, String operator) {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("target 파라미터는 비어 있을 수 없습니다.");
        }

        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("operator 파라미터는 비어 있을 수 없습니다.");
        }
    }
}
```

</details>

### 4.7 `CacheWarmupOperationTemplate.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/CacheWarmupOperationTemplate.java`
- 역할: 캐시 예열 작업의 개별 단계 구현
- 상세 설명:
- `jobType()`은 `cache-warmup`, `jobName()`은 `캐시 예열 작업`을 반환합니다.
- `target`에 `cache` 문자열이 포함되는지 검증하고, 키 목록 수집, 프리로드 요청, 모니터링 등록 단계를 구현합니다.

<details>
<summary><code>CacheWarmupOperationTemplate.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CacheWarmupOperationTemplate extends AbstractOperationTemplate {

    @Override
    public String jobType() {
        return "cache-warmup";
    }

    @Override
    public String jobName() {
        return "캐시 예열 작업";
    }

    @Override
    protected void validateTarget(String target, List<String> steps) {
        if (!target.contains("cache")) {
            throw new IllegalArgumentException("cache-warmup 작업의 target은 cache 문자열을 포함해야 합니다.");
        }
        steps.add("3. 개별 검증 단계: 캐시 대상 영역 확인 완료");
    }

    @Override
    protected void prepare(String target, String operator, List<String> steps) {
        steps.add("4. 개별 준비 단계: %s 영역의 키 목록을 수집".formatted(target));
    }

    @Override
    protected String executeCore(String target, String operator, List<String> steps) {
        steps.add("6. 개별 실행 단계: 캐시 프리로드 요청 전송");
        return "%s 영역의 데이터를 미리 적재해 초기 응답 속도를 높였습니다.".formatted(target);
    }

    @Override
    protected void afterExecute(String target, String operator, List<String> steps) {
        steps.add("7. 개별 후처리 단계: 캐시 히트율 모니터링 등록");
    }
}
```

</details>

### 4.8 `ReportPublishOperationTemplate.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/ReportPublishOperationTemplate.java`
- 역할: 보고서 발행 작업의 개별 단계 구현
- 상세 설명:
- `jobType()`은 `report-publish`, `jobName()`은 `보고서 발행 작업`을 반환합니다.
- 보고서 식별자를 검증하고, 메타데이터 로딩, PDF 생성 및 발행, 발행 이력 저장 단계를 구현합니다.

<details>
<summary><code>ReportPublishOperationTemplate.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReportPublishOperationTemplate extends AbstractOperationTemplate {

    @Override
    public String jobType() {
        return "report-publish";
    }

    @Override
    public String jobName() {
        return "보고서 발행 작업";
    }

    @Override
    protected void validateTarget(String target, List<String> steps) {
        if (target.length() < 5) {
            throw new IllegalArgumentException("report-publish 작업의 target은 5자 이상이어야 합니다.");
        }
        steps.add("3. 개별 검증 단계: 보고서 식별자와 발행 범위 확인 완료");
    }

    @Override
    protected void prepare(String target, String operator, List<String> steps) {
        steps.add("4. 개별 준비 단계: 보고서 메타데이터와 구독자 목록 로딩");
    }

    @Override
    protected String executeCore(String target, String operator, List<String> steps) {
        steps.add("6. 개별 실행 단계: PDF 생성 후 구독자에게 발행");
        return "%s 보고서를 생성하고 구독자 채널로 발행했습니다.".formatted(target);
    }

    @Override
    protected void afterExecute(String target, String operator, List<String> steps) {
        steps.add("7. 개별 후처리 단계: 발행 이력과 재시도 메타데이터 저장");
    }
}
```

</details>

### 4.9 `TemplateJobResult.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/TemplateJobResult.java`
- 역할: 템플릿 실행 내부 결과 모델
- 상세 설명:
- 내부 서비스 계층에서 사용하는 결과 객체입니다.
- 작업 식별 정보, 상태, 메시지, 단계 목록을 보관하고 응답 DTO 변환의 기준이 됩니다.

<details>
<summary><code>TemplateJobResult.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service;

import java.util.List;

public class TemplateJobResult {

    private final String jobType;
    private final String jobName;
    private final String target;
    private final String operator;
    private final String status;
    private final String resultMessage;
    private final List<String> steps;

    public TemplateJobResult(
            String jobType,
            String jobName,
            String target,
            String operator,
            String status,
            String resultMessage,
            List<String> steps
    ) {
        this.jobType = jobType;
        this.jobName = jobName;
        this.target = target;
        this.operator = operator;
        this.status = status;
        this.resultMessage = resultMessage;
        this.steps = List.copyOf(steps);
    }

    public String getJobType() {
        return jobType;
    }

    public String getJobName() {
        return jobName;
    }

    public String getTarget() {
        return target;
    }

    public String getOperator() {
        return operator;
    }

    public String getStatus() {
        return status;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public List<String> getSteps() {
        return steps;
    }
}
```

</details>

### 4.10 `TemplateOperationDispatcher.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/service/TemplateOperationDispatcher.java`
- 역할: `jobType`에 맞는 템플릿 구현체 선택 및 실행
- 상세 설명:
- 핵심 공개 메서드: `execute()`, `supportedJobTypes()`
- 스프링이 주입한 `List<AbstractOperationTemplate>`를 정렬해 맵으로 보관하고, 요청된 `jobType`에 맞는 구현체를 실행합니다.
- 새 템플릿 클래스가 추가되면 이 디스패처는 수정 없이 자동으로 목록에 포함됩니다.

<details>
<summary><code>TemplateOperationDispatcher.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TemplateOperationDispatcher {

    private final Map<String, AbstractOperationTemplate> templates;

    public TemplateOperationDispatcher(List<AbstractOperationTemplate> templateList) {
        this.templates = new LinkedHashMap<>();
        templateList.stream()
                .sorted((left, right) -> left.jobType().compareTo(right.jobType()))
                .forEach(template -> this.templates.put(template.jobType(), template));
    }

    public TemplateJobResult execute(String jobType, String target, String operator) {
        AbstractOperationTemplate template = templates.get(jobType);
        if (template == null) {
            throw new IllegalArgumentException(
                    "지원하지 않는 jobType 입니다. 사용 가능 값: " + String.join(", ", templates.keySet())
            );
        }
        return template.execute(target, operator);
    }

    public List<String> supportedJobTypes() {
        return List.copyOf(templates.keySet());
    }
}
```

</details>

### 4.11 `TemplateMethodControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task03_template_method/TemplateMethodControllerTest.java`
- 역할: `final` 템플릿 메서드, 정상 실행, 예외 흐름 검증
- 상세 설명:
- 검증 시나리오:
  - `templateMethod_execute_isFinal`
  - `jobs_returnsSupportedTemplates`
  - `runCacheWarmup_usesTemplateFlow`
  - `runReportPublish_usesDifferentOverriddenSteps`
  - `runUnknownJobType_returns400`
- 템플릿 메서드가 실제로 `final`인지 리플렉션으로 확인하고, 두 구현체의 오버라이드 결과가 서로 다르게 반영되는지도 검증합니다.

<details>
<summary><code>TemplateMethodControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class TemplateMethodControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void templateMethod_execute_isFinal() throws Exception {
        Method execute = Class.forName(
                        "com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service.AbstractOperationTemplate"
                )
                .getDeclaredMethod("execute", String.class, String.class);

        org.assertj.core.api.Assertions.assertThat(Modifier.isFinal(execute.getModifiers())).isTrue();
    }

    @Test
    void jobs_returnsSupportedTemplates() throws Exception {
        mockMvc.perform(get("/mission06/task03/template-method/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supportedJobTypes", hasSize(2)))
                .andExpect(jsonPath("$.supportedJobTypes[0]").value("cache-warmup"))
                .andExpect(jsonPath("$.supportedJobTypes[1]").value("report-publish"));
    }

    @Test
    void runCacheWarmup_usesTemplateFlow() throws Exception {
        mockMvc.perform(post("/mission06/task03/template-method/jobs/cache-warmup/run")
                        .param("target", "edge-cache")
                        .param("operator", "ops-kim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobType").value("cache-warmup"))
                .andExpect(jsonPath("$.jobName").value("캐시 예열 작업"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.steps", hasSize(8)))
                .andExpect(jsonPath("$.steps[0]").value("1. 공통 시작 단계: jobType=cache-warmup, operator=ops-kim"))
                .andExpect(jsonPath("$.steps[2]").value("3. 개별 검증 단계: 캐시 대상 영역 확인 완료"))
                .andExpect(jsonPath("$.steps[7]").value("8. 공통 종료 단계: 결과 응답 조합 완료"));
    }

    @Test
    void runReportPublish_usesDifferentOverriddenSteps() throws Exception {
        mockMvc.perform(post("/mission06/task03/template-method/jobs/report-publish/run")
                        .param("target", "monthly-sales")
                        .param("operator", "data-lee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobType").value("report-publish"))
                .andExpect(jsonPath("$.jobName").value("보고서 발행 작업"))
                .andExpect(jsonPath("$.resultMessage").value("monthly-sales 보고서를 생성하고 구독자 채널로 발행했습니다."))
                .andExpect(jsonPath("$.steps[2]").value("3. 개별 검증 단계: 보고서 식별자와 발행 범위 확인 완료"))
                .andExpect(jsonPath("$.steps[6]").value("7. 개별 후처리 단계: 발행 이력과 재시도 메타데이터 저장"));
    }

    @Test
    void runUnknownJobType_returns400() throws Exception {
        mockMvc.perform(post("/mission06/task03/template-method/jobs/unknown/run")
                        .param("target", "edge-cache")
                        .param("operator", "ops-kim"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 jobType 입니다. 사용 가능 값: cache-warmup, report-publish"));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **템플릿 메서드 패턴**
  - 핵심: 상위 클래스가 알고리즘의 뼈대를 정의하고, 하위 클래스는 특정 단계만 오버라이드합니다.
  - 왜 쓰는가: 공통 흐름을 한 곳에서 관리하면서도, 작업별 차이는 하위 클래스에만 두어 확장 비용을 줄일 수 있습니다.
  - 참고 링크:
    - https://refactoring.guru/design-patterns/template-method

- **추상 클래스와 추상 메서드**
  - 핵심: 공통 상태와 공통 메서드는 상위 클래스에 두고, 구현이 달라지는 메서드는 `abstract`로 선언합니다.
  - 왜 쓰는가: 템플릿 메서드 패턴에서 상위 클래스는 흐름을 가지고, 하위 클래스는 빠진 단계만 채워야 하기 때문입니다.
  - 참고 링크:
    - https://docs.oracle.com/javase/tutorial/java/IandI/abstract.html

- **`final` 메서드**
  - 핵심: `final`로 선언된 메서드는 하위 클래스가 오버라이드할 수 없습니다.
  - 왜 쓰는가: 템플릿 메서드의 실행 순서 자체는 바뀌면 안 되므로, `execute()`를 `final`로 고정해 패턴의 의도를 보장했습니다.
  - 참고 링크:
    - https://docs.oracle.com/javase/tutorial/java/IandI/final.html

- **스프링의 컬렉션 주입**
  - 핵심: 같은 타입의 빈 여러 개를 `List`나 `Map`으로 한 번에 주입받을 수 있습니다.
  - 왜 쓰는가: 디스패처가 등록된 모든 템플릿 구현체를 받아 `jobType` 기준으로 선택해야 하기 때문입니다.
  - 참고 링크:
    - https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 지원 작업 목록 조회

```bash
curl "http://localhost:8080/mission06/task03/template-method/jobs"
```

예상 결과:

- HTTP 200
- `supportedJobTypes=["cache-warmup","report-publish"]`
- 템플릿 실행 순서 정의 문자열 포함

### 6.3 캐시 예열 작업 실행

```bash
curl -X POST "http://localhost:8080/mission06/task03/template-method/jobs/cache-warmup/run?target=edge-cache&operator=ops-kim"
```

예상 결과:

- HTTP 200
- `jobType=cache-warmup`
- `status=SUCCESS`
- 단계 목록에 캐시 대상 검증, 키 목록 수집, 프리로드 요청, 히트율 모니터링 등록이 포함됨

### 6.4 보고서 발행 작업 실행

```bash
curl -X POST "http://localhost:8080/mission06/task03/template-method/jobs/report-publish/run?target=monthly-sales&operator=data-lee"
```

예상 결과:

- HTTP 200
- `jobType=report-publish`
- `resultMessage=monthly-sales 보고서를 생성하고 구독자 채널로 발행했습니다.`
- 단계 목록에 메타데이터 로딩, PDF 생성 및 발행, 발행 이력 저장이 포함됨

### 6.5 잘못된 작업 타입 확인

```bash
curl -X POST "http://localhost:8080/mission06/task03/template-method/jobs/unknown/run?target=edge-cache&operator=ops-kim"
```

예상 결과:

- HTTP 400
- `error=BAD_REQUEST`
- `message=지원하지 않는 jobType 입니다. 사용 가능 값: cache-warmup, report-publish`

### 6.6 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.TemplateMethodControllerTest
```

예상 결과:

- `BUILD SUCCESSFUL`
- 템플릿 메서드 `final` 여부, 두 작업 타입의 개별 단계 차이, 예외 흐름이 모두 통과합니다.

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
  - 작업 목록 조회 시 지원 작업 타입이 두 개 반환되어야 합니다.
  - `cache-warmup`, `report-publish` 실행 결과가 모두 `SUCCESS`여야 합니다.
  - 두 작업의 단계 목록은 공통 흐름은 같고, 개별 검증/준비/실행/후처리 메시지는 달라야 합니다.
  - 잘못된 작업 타입 요청은 400 오류를 반환해야 합니다.

- 응답 스냅샷 파일:
  - `docs/mission-06-spring-core-advanced/task-03-template-method/responses/job-catalog.txt`
  - `docs/mission-06-spring-core-advanced/task-03-template-method/responses/cache-warmup-success.txt`
  - `docs/mission-06-spring-core-advanced/task-03-template-method/responses/report-publish-success.txt`
  - `docs/mission-06-spring-core-advanced/task-03-template-method/responses/unknown-job-error.txt`

- 테스트 로그 파일:
  - `docs/mission-06-spring-core-advanced/task-03-template-method/task03-gradle-test-output.txt`

- 이번 태스크는 브라우저 이미지 캡처 없이 API 응답 스냅샷 텍스트로 실행 결과를 재현 가능하게 남겼습니다.

## 8. 학습 내용

- 템플릿 메서드 패턴은 "전체 알고리즘의 구조는 유지하고, 바뀌는 단계만 확장한다"는 점이 핵심입니다. 상위 클래스가 흐름을 장악하므로 하위 클래스가 공통 순서를 깨뜨릴 수 없습니다.
- `final execute()`와 `abstract validateTarget()/prepare()/executeCore()` 조합은 이 패턴을 코드로 가장 직접적으로 보여줍니다. 공통 흐름과 확장 포인트가 명확히 분리됩니다.
- 디스패처가 `List<AbstractOperationTemplate>`를 받아 `jobType`으로 매핑하는 구조를 쓰면 새 작업을 추가할 때 컨트롤러를 거의 바꾸지 않아도 됩니다. 즉, 확장에는 열려 있고 기존 호출 코드는 안정적으로 유지됩니다.
- 이번 예제는 이후 전략 패턴이나 스프링 빈 조합 구조를 이해하는 데도 도움이 됩니다. 공통 흐름을 상속으로 고정하는 방식과, 구현체 선택을 스프링이 맡는 방식을 동시에 경험할 수 있기 때문입니다.
