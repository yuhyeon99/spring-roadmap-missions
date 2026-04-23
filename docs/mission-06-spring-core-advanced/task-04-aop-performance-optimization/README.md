# 스프링 핵심 원리 - 고급: AOP 적용 시 성능 최적화 시스템 구현

이 문서는 `mission-06-spring-core-advanced`의 `task-04-aop-performance-optimization`를 기준으로 정리한 보고서입니다.
같은 비즈니스 로직에 두 종류의 AOP를 적용해, 최적화 전에는 매 호출마다 무거운 부가 작업을 수행하고, 최적화 후에는 포인트컷 범위 축소, 메서드 메타데이터 캐시, 조건부 스냅샷 생성으로 부하를 줄이도록 구성했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-06-spring-core-advanced` / `task-04-aop-performance-optimization`
- 목표:
  - AOP를 적용할 때 발생할 수 있는 성능 저하 요인을 코드로 재현합니다.
  - 동일한 핵심 로직에 대해 기본 AOP와 최적화된 AOP를 각각 적용합니다.
  - 최적화 전후 실행 시간을 비교하고, 어떤 최적화 전략이 수치 개선에 기여했는지 확인합니다.
- 엔드포인트:
  - `GET /mission06/task04/aop-performance/strategies`
  - `GET /mission06/task04/aop-performance/compare`

설계한 시스템 정의:

- 비교 대상 서비스:
  - `BaselineProjectionService`: 비효율적인 AOP 적용 대상
  - `OptimizedProjectionService`: 최적화된 AOP 적용 대상
- 공통 핵심 로직:
  - `ProjectionWorkloadExecutor`가 실제 비즈니스 계산을 담당
  - 두 서비스는 같은 로직을 호출하므로, 차이는 AOP 부가 기능에서만 발생
- 기본 AOP:
  - `BaselineProfilingAspect`
  - 매 호출마다 메서드 라벨 문자열을 새로 조합
  - 매 호출마다 무거운 payload 스냅샷 생성
- 최적화 AOP:
  - `OptimizedProfilingAspect`
  - `@OptimizedTrace` 메서드만 포인트컷으로 지정
  - `ConcurrentHashMap`으로 메서드 라벨 캐시
  - 느린 호출일 때만 payload 스냅샷 생성
- 비교 서비스:
  - `AopPerformanceComparisonService`
  - 워밍업 후 반복 실행
  - 총 실행 시간, 호출 횟수, 스냅샷 횟수, 캐시 hit/miss를 응답으로 제공

핵심 설계 규칙:

1. 성능 비교가 왜곡되지 않도록 비즈니스 로직은 하나의 실행기(`ProjectionWorkloadExecutor`)로 통일했습니다.
2. 기본 버전과 최적화 버전의 차이는 AOP 부가 기능 처리 방식에만 두었습니다.
3. 최적화 포인트는 눈으로 확인할 수 있는 수치로 노출되도록 메트릭 저장 컴포넌트를 분리했습니다.
4. 비교 API 하나만 호출해도 워밍업, 측정, 결과 정리가 한 번에 끝나도록 설계했습니다.

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/controller/AopPerformanceOptimizationController.java` | 최적화 전략 조회와 성능 비교 API 제공 |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/controller/AopPerformanceExceptionHandler.java` | 잘못된 비교 파라미터를 400 JSON 응답으로 변환 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/dto/AopOptimizationStrategyResponse.java` | 적용한 최적화 전략 목록 응답 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/dto/AopPerformanceComparisonResponse.java` | 최적화 전후 측정값과 메트릭 응답 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/dto/AopPerformanceErrorResponse.java` | 오류 응답 구조 정의 |
| Annotation | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/annotation/BaselineTrace.java` | 기본 AOP 대상 메서드 표시 |
| Annotation | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/annotation/OptimizedTrace.java` | 최적화 AOP 대상 메서드 표시와 느린 호출 기준 제공 |
| Aspect | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/aspect/BaselineProfilingAspect.java` | 매 호출마다 무거운 스냅샷을 생성하는 기본 애스펙트 |
| Aspect | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/aspect/OptimizedProfilingAspect.java` | 캐시와 조건부 스냅샷을 적용한 최적화 애스펙트 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/service/AopPerformanceComparisonService.java` | 워밍업, 반복 측정, 비교 결과 조합 담당 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/service/BaselineProjectionService.java` | 기본 AOP가 적용되는 서비스 진입점 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/service/OptimizedProjectionService.java` | 최적화 AOP가 적용되는 서비스 진입점 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/service/ProjectionWorkloadExecutor.java` | 두 서비스가 공통으로 사용하는 실제 비즈니스 계산 로직 |
| Config | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/support/BaselineAspectMetrics.java` | 기본 애스펙트의 호출/스냅샷 메트릭 저장 |
| Config | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/support/OptimizedAspectMetrics.java` | 최적화 애스펙트의 호출/스냅샷/캐시 메트릭 저장 |
| Config | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/support/ExpensivePayloadFormatter.java` | 비용이 큰 payload 스냅샷 생성 로직 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/AopPerformanceOptimizationControllerTest.java` | 프록시 적용, 성능 비교, API 응답, 예외 흐름 검증 |

## 3. 구현 단계와 주요 코드 해설

1. 동일한 핵심 로직을 두 서비스가 공유하도록 `ProjectionWorkloadExecutor`를 만들었습니다.
   - 문자열 payload를 입력받아 checksum, 모음 개수, 미리보기 문자열을 계산합니다.
   - 기본 버전과 최적화 버전 모두 이 로직만 호출하므로, 비교 시 핵심 로직 차이가 개입하지 않습니다.

2. 성능 저하가 있는 기본 애스펙트를 `BaselineProfilingAspect`로 구현했습니다.
   - 메서드가 호출될 때마다 리플렉션 정보를 이용해 긴 메서드 라벨을 새로 만듭니다.
   - `ExpensivePayloadFormatter`를 바로 실행해 무거운 snapshot 문자열을 생성합니다.
   - 이 snapshot은 실제 로그를 남기지 않더라도 digest로 저장해, JIT 최적화로 사라지지 않게 했습니다.

3. 최적화 애스펙트를 `OptimizedProfilingAspect`로 분리했습니다.
   - `@OptimizedTrace`가 붙은 메서드만 추적합니다.
   - 메서드 라벨은 `OptimizedAspectMetrics` 내부 `ConcurrentHashMap` 캐시를 사용합니다.
   - 호출 시간이 `slowThresholdNanos`를 넘는 경우에만 무거운 snapshot을 생성합니다.

4. 비교 서비스에서 워밍업과 반복 측정을 한 번에 처리했습니다.
   - `warmUp()`에서 두 서비스를 미리 몇 번 실행해 첫 호출 편차를 줄였습니다.
   - 그 뒤 메트릭을 초기화하고, 동일한 payload로 `iterations`만큼 반복 호출합니다.
   - 총 실행 시간, 호출 수, snapshot 수, cache hit/miss, 결과 동일성을 응답 DTO로 반환합니다.

5. 컨트롤러와 예외 처리 클래스로 제출용 API를 구성했습니다.
   - `GET /strategies`는 어떤 최적화를 적용했는지 설명합니다.
   - `GET /compare`는 실제 성능 수치를 보여줍니다.
   - `iterations <= 0`, `payloadSize < 64` 같은 잘못된 입력은 400 응답으로 통일했습니다.

요청 흐름 요약:

1. 클라이언트가 `/compare`에 반복 횟수와 payload 크기를 전달합니다.
2. `AopPerformanceComparisonService`가 payload를 생성하고 두 서비스 워밍업을 수행합니다.
3. 기본 애스펙트와 최적화 애스펙트가 각각 같은 비즈니스 로직을 감싼 채 반복 호출됩니다.
4. 각 애스펙트가 메트릭 저장소에 호출 수, snapshot 수, 캐시 hit/miss를 기록합니다.
5. 최종 비교 결과가 `AopPerformanceComparisonResponse`로 정리되어 반환됩니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `AopPerformanceOptimizationController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/controller/AopPerformanceOptimizationController.java`
- 역할: 최적화 전략 조회와 성능 비교 API 제공
- 상세 설명:
- 기본 경로: `/mission06/task04/aop-performance`
- 매핑 메서드:
  - `GET /strategies` -> 적용한 성능 최적화 전략 조회
  - `GET /compare` -> 최적화 전후 성능 수치 비교
- 컨트롤러는 모든 계산을 서비스에 위임하고, 요청 파라미터 `iterations`, `payloadSize`를 그대로 비교 로직에 전달합니다.

<details>
<summary><code>AopPerformanceOptimizationController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopOptimizationStrategyResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopPerformanceComparisonResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service.AopPerformanceComparisonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task04/aop-performance")
public class AopPerformanceOptimizationController {

    private final AopPerformanceComparisonService aopPerformanceComparisonService;

    public AopPerformanceOptimizationController(AopPerformanceComparisonService aopPerformanceComparisonService) {
        this.aopPerformanceComparisonService = aopPerformanceComparisonService;
    }

    @GetMapping("/strategies")
    public AopOptimizationStrategyResponse strategies() {
        return aopPerformanceComparisonService.describeStrategies();
    }

    @GetMapping("/compare")
    public AopPerformanceComparisonResponse compare(
            @RequestParam(defaultValue = "120") int iterations,
            @RequestParam(defaultValue = "480") int payloadSize
    ) {
        return aopPerformanceComparisonService.compare(iterations, payloadSize);
    }
}
```

</details>

### 4.2 `AopPerformanceExceptionHandler.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/controller/AopPerformanceExceptionHandler.java`
- 역할: 잘못된 비교 파라미터를 400 JSON 응답으로 변환
- 상세 설명:
- `IllegalArgumentException`을 HTTP 400으로 통일합니다.
- 경로와 메시지를 함께 내려주므로 어떤 요청이 왜 실패했는지 바로 확인할 수 있습니다.

<details>
<summary><code>AopPerformanceExceptionHandler.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopPerformanceErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AopPerformanceOptimizationController.class)
public class AopPerformanceExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public AopPerformanceErrorResponse handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new AopPerformanceErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
```

</details>

### 4.3 `AopOptimizationStrategyResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/dto/AopOptimizationStrategyResponse.java`
- 역할: 적용한 최적화 전략 목록 응답
- 상세 설명:
- 시스템 목표와 전략 목록을 함께 담습니다.
- `/strategies` 응답에서 사용되며, 성능 개선 의도를 설명하는 문서 역할도 함께 수행합니다.

<details>
<summary><code>AopOptimizationStrategyResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto;

import java.util.List;

public class AopOptimizationStrategyResponse {

    private final String missionGoal;
    private final List<String> strategies;

    public AopOptimizationStrategyResponse(String missionGoal, List<String> strategies) {
        this.missionGoal = missionGoal;
        this.strategies = List.copyOf(strategies);
    }

    public String getMissionGoal() {
        return missionGoal;
    }

    public List<String> getStrategies() {
        return strategies;
    }
}
```

</details>

### 4.4 `AopPerformanceComparisonResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/dto/AopPerformanceComparisonResponse.java`
- 역할: 최적화 전후 측정값과 메트릭 응답
- 상세 설명:
- 총 실행 시간, 호출당 평균 시간, 향상 비율, snapshot 횟수, cache hit/miss, 결과 동일 여부를 모두 포함합니다.
- 생성자에서 `baselinePerCallMicros`, `optimizedPerCallMicros`, `improvementPercent`를 계산해 응답만 봐도 개선 폭을 이해할 수 있습니다.

<details>
<summary><code>AopPerformanceComparisonResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto;

import java.util.List;

public class AopPerformanceComparisonResponse {

    private final int iterations;
    private final int payloadSize;
    private final long baselineElapsedNanos;
    private final long optimizedElapsedNanos;
    private final double baselinePerCallMicros;
    private final double optimizedPerCallMicros;
    private final double improvementPercent;
    private final long baselineInvocationCount;
    private final long baselineSnapshotCount;
    private final long optimizedInvocationCount;
    private final long optimizedSnapshotCount;
    private final long optimizedMetadataCacheHitCount;
    private final long optimizedMetadataCacheMissCount;
    private final boolean resultEquality;
    private final String projectionPreview;
    private final List<String> optimizationStrategies;

    public AopPerformanceComparisonResponse(
            int iterations,
            int payloadSize,
            long baselineElapsedNanos,
            long optimizedElapsedNanos,
            long baselineInvocationCount,
            long baselineSnapshotCount,
            long optimizedInvocationCount,
            long optimizedSnapshotCount,
            long optimizedMetadataCacheHitCount,
            long optimizedMetadataCacheMissCount,
            boolean resultEquality,
            String projectionPreview,
            List<String> optimizationStrategies
    ) {
        this.iterations = iterations;
        this.payloadSize = payloadSize;
        this.baselineElapsedNanos = baselineElapsedNanos;
        this.optimizedElapsedNanos = optimizedElapsedNanos;
        this.baselinePerCallMicros = nanosToMicros(baselineElapsedNanos, iterations);
        this.optimizedPerCallMicros = nanosToMicros(optimizedElapsedNanos, iterations);
        this.improvementPercent = calculateImprovementPercent(baselineElapsedNanos, optimizedElapsedNanos);
        this.baselineInvocationCount = baselineInvocationCount;
        this.baselineSnapshotCount = baselineSnapshotCount;
        this.optimizedInvocationCount = optimizedInvocationCount;
        this.optimizedSnapshotCount = optimizedSnapshotCount;
        this.optimizedMetadataCacheHitCount = optimizedMetadataCacheHitCount;
        this.optimizedMetadataCacheMissCount = optimizedMetadataCacheMissCount;
        this.resultEquality = resultEquality;
        this.projectionPreview = projectionPreview;
        this.optimizationStrategies = List.copyOf(optimizationStrategies);
    }

    private double nanosToMicros(long nanos, int iterations) {
        return nanos / (double) iterations / 1_000.0;
    }

    private double calculateImprovementPercent(long baseline, long optimized) {
        if (baseline <= 0L) {
            return 0.0;
        }
        return ((baseline - optimized) / (double) baseline) * 100.0;
    }

    public int getIterations() {
        return iterations;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public long getBaselineElapsedNanos() {
        return baselineElapsedNanos;
    }

    public long getOptimizedElapsedNanos() {
        return optimizedElapsedNanos;
    }

    public double getBaselinePerCallMicros() {
        return baselinePerCallMicros;
    }

    public double getOptimizedPerCallMicros() {
        return optimizedPerCallMicros;
    }

    public double getImprovementPercent() {
        return improvementPercent;
    }

    public long getBaselineInvocationCount() {
        return baselineInvocationCount;
    }

    public long getBaselineSnapshotCount() {
        return baselineSnapshotCount;
    }

    public long getOptimizedInvocationCount() {
        return optimizedInvocationCount;
    }

    public long getOptimizedSnapshotCount() {
        return optimizedSnapshotCount;
    }

    public long getOptimizedMetadataCacheHitCount() {
        return optimizedMetadataCacheHitCount;
    }

    public long getOptimizedMetadataCacheMissCount() {
        return optimizedMetadataCacheMissCount;
    }

    public boolean isResultEquality() {
        return resultEquality;
    }

    public String getProjectionPreview() {
        return projectionPreview;
    }

    public List<String> getOptimizationStrategies() {
        return optimizationStrategies;
    }
}
```

</details>

### 4.5 `AopPerformanceErrorResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/dto/AopPerformanceErrorResponse.java`
- 역할: 오류 응답 구조 정의
- 상세 설명:
- 상태 코드, 에러 키, 메시지, 요청 경로를 고정된 구조로 제공합니다.
- 파라미터 검증 실패 시 프론트나 테스트 코드가 일관된 방식으로 해석할 수 있게 만듭니다.

<details>
<summary><code>AopPerformanceErrorResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto;

public class AopPerformanceErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public AopPerformanceErrorResponse(int status, String error, String message, String path) {
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

### 4.6 `BaselineTrace.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/annotation/BaselineTrace.java`
- 역할: 기본 AOP 대상 메서드 표시
- 상세 설명:
- 기본 애스펙트가 어떤 메서드를 감쌀지 구분하는 마커 애너테이션입니다.
- 속성 없이 단순히 “비효율적인 AOP 비교군”이라는 의미만 전달합니다.

<details>
<summary><code>BaselineTrace.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaselineTrace {
}
```

</details>

### 4.7 `OptimizedTrace.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/annotation/OptimizedTrace.java`
- 역할: 최적화 AOP 대상 메서드 표시와 느린 호출 기준 제공
- 상세 설명:
- 최적화 애스펙트의 적용 대상을 지정합니다.
- `slowThresholdNanos` 속성으로 “느린 호출” 기준을 메서드별로 조정할 수 있게 열어두었습니다.

<details>
<summary><code>OptimizedTrace.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptimizedTrace {

    long slowThresholdNanos() default 500_000L;
}
```

</details>

### 4.8 `BaselineProfilingAspect.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/aspect/BaselineProfilingAspect.java`
- 역할: 매 호출마다 무거운 스냅샷을 생성하는 기본 애스펙트
- 상세 설명:
- `@BaselineTrace`가 붙은 메서드 호출 전후를 `@Around`로 감쌉니다.
- 메서드 라벨을 매번 새로 조합하고, `ExpensivePayloadFormatter`를 무조건 실행합니다.
- 결과적으로 실제 업무 로직과 무관한 비용이 매 요청마다 반복되도록 의도적으로 설계했습니다.

<details>
<summary><code>BaselineProfilingAspect.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.aspect;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.BaselineAspectMetrics;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.ExpensivePayloadFormatter;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BaselineProfilingAspect {

    private static final int SNAPSHOT_ROUNDS = 36;

    private final ExpensivePayloadFormatter expensivePayloadFormatter;
    private final BaselineAspectMetrics baselineAspectMetrics;

    public BaselineProfilingAspect(
            ExpensivePayloadFormatter expensivePayloadFormatter,
            BaselineAspectMetrics baselineAspectMetrics
    ) {
        this.expensivePayloadFormatter = expensivePayloadFormatter;
        this.baselineAspectMetrics = baselineAspectMetrics;
    }

    @Around("@annotation(com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation.BaselineTrace) && args(payload)")
    public Object profile(ProceedingJoinPoint joinPoint, String payload) throws Throwable {
        long start = System.nanoTime();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodLabel = buildVerboseMethodLabel(method);
        String snapshot = expensivePayloadFormatter.createSnapshot(methodLabel, payload, SNAPSHOT_ROUNDS);
        baselineAspectMetrics.recordSnapshot(snapshot);

        try {
            return joinPoint.proceed();
        } finally {
            baselineAspectMetrics.recordInvocation(System.nanoTime() - start);
        }
    }

    private String buildVerboseMethodLabel(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getDeclaringClass().getName())
                .append("#")
                .append(method.getName())
                .append("(");

        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int index = 0; index < parameterTypes.length; index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(parameterTypes[index].getTypeName());
        }

        builder.append(")");
        return builder.toString();
    }
}
```

</details>

### 4.9 `OptimizedProfilingAspect.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/aspect/OptimizedProfilingAspect.java`
- 역할: 캐시와 조건부 스냅샷을 적용한 최적화 애스펙트
- 상세 설명:
- `@OptimizedTrace` 애너테이션을 바인딩해 느린 호출 기준을 함께 읽습니다.
- 메서드 라벨은 캐시를 통해 재사용하고, 실제 호출 시간이 임계값 이상일 때만 무거운 snapshot을 생성합니다.
- 동일한 부가 기능 목적을 유지하면서도 “항상 비싼 작업 수행”을 피하도록 만든 버전입니다.

<details>
<summary><code>OptimizedProfilingAspect.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.aspect;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation.OptimizedTrace;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.ExpensivePayloadFormatter;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.OptimizedAspectMetrics;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OptimizedProfilingAspect {

    private static final int SNAPSHOT_ROUNDS = 36;

    private final ExpensivePayloadFormatter expensivePayloadFormatter;
    private final OptimizedAspectMetrics optimizedAspectMetrics;

    public OptimizedProfilingAspect(
            ExpensivePayloadFormatter expensivePayloadFormatter,
            OptimizedAspectMetrics optimizedAspectMetrics
    ) {
        this.expensivePayloadFormatter = expensivePayloadFormatter;
        this.optimizedAspectMetrics = optimizedAspectMetrics;
    }

    @Around("@annotation(optimizedTrace) && args(payload)")
    public Object profile(
            ProceedingJoinPoint joinPoint,
            OptimizedTrace optimizedTrace,
            String payload
    ) throws Throwable {
        long start = System.nanoTime();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodLabel = optimizedAspectMetrics.resolveMethodLabel(method);

        try {
            return joinPoint.proceed();
        } finally {
            long observedNanos = System.nanoTime() - start;
            optimizedAspectMetrics.recordInvocation(observedNanos);

            if (observedNanos >= optimizedTrace.slowThresholdNanos()) {
                String snapshot = expensivePayloadFormatter.createSnapshot(methodLabel, payload, SNAPSHOT_ROUNDS);
                optimizedAspectMetrics.recordSnapshot(snapshot);
            }
        }
    }
}
```

</details>

### 4.10 `AopPerformanceComparisonService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/service/AopPerformanceComparisonService.java`
- 역할: 워밍업, 반복 측정, 비교 결과 조합 담당
- 상세 설명:
- 핵심 공개 메서드:
  - `describeStrategies()` -> 적용한 최적화 전략 설명
  - `compare()` -> 워밍업, 메트릭 초기화, 반복 실행, 응답 조합
- `compare()`는 잘못된 입력을 검증하고, 동일 payload로 두 서비스를 측정합니다.
- 서비스가 비교 로직을 책임지므로 컨트롤러는 단순 입출력 계층으로 유지됩니다.

<details>
<summary><code>AopPerformanceComparisonService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopOptimizationStrategyResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopPerformanceComparisonResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.BaselineAspectMetrics;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.OptimizedAspectMetrics;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AopPerformanceComparisonService {

    private static final List<String> OPTIMIZATION_STRATEGIES = List.of(
            "포인트컷을 @OptimizedTrace 메서드로 좁혀 비교 대상 메서드만 추적합니다.",
            "메서드 라벨은 ConcurrentHashMap에 캐시해 매 호출마다 리플렉션 문자열 조합을 반복하지 않습니다.",
            "비용이 큰 payload 스냅샷은 느린 호출에서만 생성해 일반 호출의 AOP 부하를 줄입니다."
    );

    private final BaselineProjectionService baselineProjectionService;
    private final OptimizedProjectionService optimizedProjectionService;
    private final BaselineAspectMetrics baselineAspectMetrics;
    private final OptimizedAspectMetrics optimizedAspectMetrics;

    public AopPerformanceComparisonService(
            BaselineProjectionService baselineProjectionService,
            OptimizedProjectionService optimizedProjectionService,
            BaselineAspectMetrics baselineAspectMetrics,
            OptimizedAspectMetrics optimizedAspectMetrics
    ) {
        this.baselineProjectionService = baselineProjectionService;
        this.optimizedProjectionService = optimizedProjectionService;
        this.baselineAspectMetrics = baselineAspectMetrics;
        this.optimizedAspectMetrics = optimizedAspectMetrics;
    }

    public AopOptimizationStrategyResponse describeStrategies() {
        return new AopOptimizationStrategyResponse(
                "AOP 전후 부가 기능은 유지하되, 반복 리플렉션과 불필요한 스냅샷 생성을 줄여 성능 저하를 완화합니다.",
                OPTIMIZATION_STRATEGIES
        );
    }

    public AopPerformanceComparisonResponse compare(int iterations, int payloadSize) {
        validate(iterations, payloadSize);

        String payload = createPayload(payloadSize);
        warmUp(payload, Math.max(8, Math.min(20, iterations / 5)));
        baselineAspectMetrics.reset();
        optimizedAspectMetrics.reset();

        BenchmarkOutcome baselineOutcome = benchmark(iterations, payload, baselineProjectionService::buildProjection);
        BenchmarkOutcome optimizedOutcome = benchmark(iterations, payload, optimizedProjectionService::buildProjection);

        return new AopPerformanceComparisonResponse(
                iterations,
                payloadSize,
                baselineOutcome.elapsedNanos(),
                optimizedOutcome.elapsedNanos(),
                baselineAspectMetrics.invocationCount(),
                baselineAspectMetrics.snapshotCount(),
                optimizedAspectMetrics.invocationCount(),
                optimizedAspectMetrics.snapshotCount(),
                optimizedAspectMetrics.metadataCacheHitCount(),
                optimizedAspectMetrics.metadataCacheMissCount(),
                baselineOutcome.lastProjection().equals(optimizedOutcome.lastProjection()),
                optimizedOutcome.lastProjection(),
                OPTIMIZATION_STRATEGIES
        );
    }

    private void validate(int iterations, int payloadSize) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("iterations는 1 이상이어야 합니다.");
        }
        if (payloadSize < 64) {
            throw new IllegalArgumentException("payloadSize는 64 이상이어야 합니다.");
        }
    }

    private void warmUp(String payload, int warmupIterations) {
        for (int index = 0; index < warmupIterations; index++) {
            baselineProjectionService.buildProjection(payload);
            optimizedProjectionService.buildProjection(payload);
        }
    }

    private BenchmarkOutcome benchmark(int iterations, String payload, ProjectionRunner projectionRunner) {
        long start = System.nanoTime();
        String lastProjection = "";

        for (int index = 0; index < iterations; index++) {
            lastProjection = projectionRunner.run(payload);
        }

        return new BenchmarkOutcome(System.nanoTime() - start, lastProjection);
    }

    private String createPayload(int payloadSize) {
        String seed = "mission06-aop-performance-optimization-";
        StringBuilder builder = new StringBuilder(payloadSize);
        while (builder.length() < payloadSize) {
            builder.append(seed);
        }
        return builder.substring(0, payloadSize);
    }

    @FunctionalInterface
    private interface ProjectionRunner {
        String run(String payload);
    }

    private record BenchmarkOutcome(long elapsedNanos, String lastProjection) {
    }
}
```

</details>

### 4.11 `BaselineProjectionService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/service/BaselineProjectionService.java`
- 역할: 기본 AOP가 적용되는 서비스 진입점
- 상세 설명:
- `@BaselineTrace`를 붙여 기본 애스펙트 비교군에 포함시켰습니다.
- 실제 계산은 `ProjectionWorkloadExecutor`에 위임하므로, 이 클래스는 “AOP가 걸리는 경계” 역할을 담당합니다.

<details>
<summary><code>BaselineProjectionService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation.BaselineTrace;
import org.springframework.stereotype.Service;

@Service
public class BaselineProjectionService {

    private final ProjectionWorkloadExecutor projectionWorkloadExecutor;

    public BaselineProjectionService(ProjectionWorkloadExecutor projectionWorkloadExecutor) {
        this.projectionWorkloadExecutor = projectionWorkloadExecutor;
    }

    @BaselineTrace
    public String buildProjection(String payload) {
        return projectionWorkloadExecutor.buildProjection(payload);
    }
}
```

</details>

### 4.12 `OptimizedProjectionService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/service/OptimizedProjectionService.java`
- 역할: 최적화 AOP가 적용되는 서비스 진입점
- 상세 설명:
- `@OptimizedTrace`가 붙어 최적화 애스펙트가 감싸는 진입점입니다.
- 핵심 비즈니스 계산은 기본 서비스와 똑같이 `ProjectionWorkloadExecutor`에 위임합니다.

<details>
<summary><code>OptimizedProjectionService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation.OptimizedTrace;
import org.springframework.stereotype.Service;

@Service
public class OptimizedProjectionService {

    private final ProjectionWorkloadExecutor projectionWorkloadExecutor;

    public OptimizedProjectionService(ProjectionWorkloadExecutor projectionWorkloadExecutor) {
        this.projectionWorkloadExecutor = projectionWorkloadExecutor;
    }

    @OptimizedTrace
    public String buildProjection(String payload) {
        return projectionWorkloadExecutor.buildProjection(payload);
    }
}
```

</details>

### 4.13 `ProjectionWorkloadExecutor.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/service/ProjectionWorkloadExecutor.java`
- 역할: 두 서비스가 공통으로 사용하는 실제 비즈니스 계산 로직
- 상세 설명:
- payload를 받아 checksum, 모음 개수, 미리보기 문자열을 계산합니다.
- 비어 있는 입력은 `IllegalArgumentException`으로 막습니다.
- 기본 서비스와 최적화 서비스가 이 로직을 공유하므로, 결과 동일 여부를 신뢰할 수 있습니다.

<details>
<summary><code>ProjectionWorkloadExecutor.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service;

import org.springframework.stereotype.Component;

@Component
public class ProjectionWorkloadExecutor {

    public String buildProjection(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("payload는 비어 있을 수 없습니다.");
        }

        long checksum = 23L;
        int vowelCount = 0;

        for (int round = 0; round < 6; round++) {
            for (int index = 0; index < payload.length(); index++) {
                char current = payload.charAt(index);
                checksum = (checksum * 33L) + current + round;
                if (isVowel(current)) {
                    vowelCount++;
                }
            }
            checksum ^= (checksum >>> 9);
        }

        String preview = payload.substring(0, Math.min(10, payload.length()));
        String suffix = payload.substring(Math.max(0, payload.length() - Math.min(10, payload.length())));
        return "projection[length=%d, checksum=%s, vowels=%d, preview=%s...%s]"
                .formatted(payload.length(), Long.toUnsignedString(checksum, 16), vowelCount, preview, suffix);
    }

    private boolean isVowel(char value) {
        return switch (Character.toLowerCase(value)) {
            case 'a', 'e', 'i', 'o', 'u' -> true;
            default -> false;
        };
    }
}
```

</details>

### 4.14 `BaselineAspectMetrics.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/support/BaselineAspectMetrics.java`
- 역할: 기본 애스펙트의 호출/스냅샷 메트릭 저장
- 상세 설명:
- 호출 횟수, snapshot 횟수, 관측 시간 누적값, snapshot digest를 저장합니다.
- `reset()`으로 비교 시작 전에 메트릭을 비웁니다.

<details>
<summary><code>BaselineAspectMetrics.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class BaselineAspectMetrics {

    private final AtomicLong invocationCount = new AtomicLong();
    private final AtomicLong snapshotCount = new AtomicLong();
    private final AtomicLong totalObservedNanos = new AtomicLong();
    private final AtomicLong snapshotDigest = new AtomicLong();

    public void recordInvocation(long observedNanos) {
        invocationCount.incrementAndGet();
        totalObservedNanos.addAndGet(observedNanos);
    }

    public void recordSnapshot(String snapshot) {
        snapshotCount.incrementAndGet();
        snapshotDigest.addAndGet(snapshot.hashCode());
    }

    public long invocationCount() {
        return invocationCount.get();
    }

    public long snapshotCount() {
        return snapshotCount.get();
    }

    public long totalObservedNanos() {
        return totalObservedNanos.get();
    }

    public long snapshotDigest() {
        return snapshotDigest.get();
    }

    public void reset() {
        invocationCount.set(0L);
        snapshotCount.set(0L);
        totalObservedNanos.set(0L);
        snapshotDigest.set(0L);
    }
}
```

</details>

### 4.15 `OptimizedAspectMetrics.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/support/OptimizedAspectMetrics.java`
- 역할: 최적화 애스펙트의 호출/스냅샷/캐시 메트릭 저장
- 상세 설명:
- 호출 수와 snapshot 수뿐 아니라 메서드 라벨 캐시 hit/miss까지 저장합니다.
- `resolveMethodLabel()`이 캐시 조회와 생성 책임을 함께 가지므로, 애스펙트 쪽 코드는 가볍게 유지됩니다.

<details>
<summary><code>OptimizedAspectMetrics.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class OptimizedAspectMetrics {

    private final AtomicLong invocationCount = new AtomicLong();
    private final AtomicLong snapshotCount = new AtomicLong();
    private final AtomicLong totalObservedNanos = new AtomicLong();
    private final AtomicLong snapshotDigest = new AtomicLong();
    private final AtomicLong metadataCacheHitCount = new AtomicLong();
    private final AtomicLong metadataCacheMissCount = new AtomicLong();
    private final ConcurrentHashMap<Method, String> methodLabelCache = new ConcurrentHashMap<>();

    public void recordInvocation(long observedNanos) {
        invocationCount.incrementAndGet();
        totalObservedNanos.addAndGet(observedNanos);
    }

    public void recordSnapshot(String snapshot) {
        snapshotCount.incrementAndGet();
        snapshotDigest.addAndGet(snapshot.hashCode());
    }

    public String resolveMethodLabel(Method method) {
        String cachedLabel = methodLabelCache.get(method);
        if (cachedLabel != null) {
            metadataCacheHitCount.incrementAndGet();
            return cachedLabel;
        }

        metadataCacheMissCount.incrementAndGet();
        String createdLabel = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        methodLabelCache.put(method, createdLabel);
        return createdLabel;
    }

    public long invocationCount() {
        return invocationCount.get();
    }

    public long snapshotCount() {
        return snapshotCount.get();
    }

    public long totalObservedNanos() {
        return totalObservedNanos.get();
    }

    public long snapshotDigest() {
        return snapshotDigest.get();
    }

    public long metadataCacheHitCount() {
        return metadataCacheHitCount.get();
    }

    public long metadataCacheMissCount() {
        return metadataCacheMissCount.get();
    }

    public void reset() {
        invocationCount.set(0L);
        snapshotCount.set(0L);
        totalObservedNanos.set(0L);
        snapshotDigest.set(0L);
        metadataCacheHitCount.set(0L);
        metadataCacheMissCount.set(0L);
        methodLabelCache.clear();
    }
}
```

</details>

### 4.16 `ExpensivePayloadFormatter.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/support/ExpensivePayloadFormatter.java`
- 역할: 비용이 큰 payload 스냅샷 생성 로직
- 상세 설명:
- payload와 메서드 라벨을 여러 라운드 반복 계산해, 일부러 비싼 보조 작업을 만듭니다.
- 기본 애스펙트는 이 로직을 항상 호출하고, 최적화 애스펙트는 느린 호출에서만 호출합니다.

<details>
<summary><code>ExpensivePayloadFormatter.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support;

import org.springframework.stereotype.Component;

@Component
public class ExpensivePayloadFormatter {

    public String createSnapshot(String methodLabel, String payload, int rounds) {
        long checksum = 17L;
        int labelLength = methodLabel.length();

        for (int round = 0; round < rounds; round++) {
            for (int index = 0; index < payload.length(); index++) {
                char payloadChar = payload.charAt(index);
                char labelChar = methodLabel.charAt(index % labelLength);
                checksum = (checksum * 131L) ^ (payloadChar + labelChar + round);
                checksum ^= (checksum << 7);
                checksum += payloadChar * (index + 3L);
            }
            checksum ^= (checksum >>> 11);
        }

        return methodLabel + "#" + payload.length() + "#" + Long.toUnsignedString(checksum, 16);
    }
}
```

</details>

### 4.17 `AopPerformanceOptimizationControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task04_aop_performance_optimization/AopPerformanceOptimizationControllerTest.java`
- 역할: 프록시 적용, 성능 비교, API 응답, 예외 흐름 검증
- 상세 설명:
- 검증 시나리오:
  - 두 서비스가 실제 Spring AOP 프록시인지 확인
  - 최적화 버전이 기본 버전보다 빠르고 snapshot 수가 적은지 확인
  - 비교 API와 전략 API의 JSON 응답 구조 확인
  - 잘못된 파라미터에 대한 400 응답 확인
- 정상 흐름과 예외 흐름을 함께 보장하는 task04 전용 통합 테스트입니다.

<details>
<summary><code>AopPerformanceOptimizationControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopPerformanceComparisonResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service.AopPerformanceComparisonService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service.BaselineProjectionService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service.OptimizedProjectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AopPerformanceOptimizationControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BaselineProjectionService baselineProjectionService;

    @Autowired
    private OptimizedProjectionService optimizedProjectionService;

    @Autowired
    private AopPerformanceComparisonService aopPerformanceComparisonService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void projectionServices_areProxiedBySpringAop() {
        assertThat(AopUtils.isAopProxy(baselineProjectionService)).isTrue();
        assertThat(AopUtils.isAopProxy(optimizedProjectionService)).isTrue();
    }

    @Test
    void compare_returnsFasterResultForOptimizedAspect() {
        AopPerformanceComparisonResponse response = aopPerformanceComparisonService.compare(120, 480);

        assertThat(response.isResultEquality()).isTrue();
        assertThat(response.getBaselineInvocationCount()).isEqualTo(120);
        assertThat(response.getOptimizedInvocationCount()).isEqualTo(120);
        assertThat(response.getBaselineSnapshotCount()).isEqualTo(120);
        assertThat(response.getOptimizedSnapshotCount()).isLessThan(response.getBaselineSnapshotCount());
        assertThat(response.getOptimizedMetadataCacheMissCount()).isEqualTo(1L);
        assertThat(response.getOptimizedMetadataCacheHitCount()).isEqualTo(119L);
        assertThat(response.getOptimizedElapsedNanos()).isLessThan(response.getBaselineElapsedNanos());
        assertThat(response.getImprovementPercent()).isGreaterThan(15.0);
    }

    @Test
    void compareEndpoint_returnsMeasuredMetrics() throws Exception {
        mockMvc.perform(get("/mission06/task04/aop-performance/compare")
                        .param("iterations", "80")
                        .param("payloadSize", "320"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iterations").value(80))
                .andExpect(jsonPath("$.payloadSize").value(320))
                .andExpect(jsonPath("$.baselineSnapshotCount").value(80))
                .andExpect(jsonPath("$.resultEquality").value(true))
                .andExpect(jsonPath("$.optimizationStrategies", hasSize(3)));
    }

    @Test
    void strategiesEndpoint_returnsOptimizationPlan() throws Exception {
        mockMvc.perform(get("/mission06/task04/aop-performance/strategies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategies", hasSize(3)))
                .andExpect(jsonPath("$.strategies[0]").value("포인트컷을 @OptimizedTrace 메서드로 좁혀 비교 대상 메서드만 추적합니다."));
    }

    @Test
    void compareEndpoint_withInvalidIterations_returns400() throws Exception {
        mockMvc.perform(get("/mission06/task04/aop-performance/compare")
                        .param("iterations", "0")
                        .param("payloadSize", "320"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("iterations는 1 이상이어야 합니다."));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 애너테이션 기반 Spring AOP 포인트컷

- 핵심:
  - `@Around("@annotation(...)")` 형태로 특정 애너테이션이 붙은 메서드만 선택해 부가 기능을 적용할 수 있습니다.
- 왜 쓰는가:
  - 모든 메서드를 넓게 감싸면 불필요한 조인 포인트 탐색과 부가 작업이 늘어납니다.
  - task04에서는 `@BaselineTrace`, `@OptimizedTrace`를 분리해 비교군과 최적화군의 적용 범위를 명확히 구분했습니다.
- 참고 링크:
  - Spring Framework Reference - AOP: `https://docs.spring.io/spring-framework/reference/core/aop.html`

### 5.2 메서드 메타데이터 캐시와 `ConcurrentHashMap`

- 핵심:
  - 같은 메서드를 여러 번 호출할 때 리플렉션 기반 문자열 조합을 매번 새로 하지 않고 캐시에 저장해 재사용합니다.
- 왜 쓰는가:
  - AOP는 공통 로직이라 호출 빈도가 높을 수 있습니다.
  - task04에서는 메서드 라벨을 `ConcurrentHashMap<Method, String>`에 저장해 hit/miss를 수치로 확인할 수 있게 했습니다.
- 참고 링크:
  - Java SE 25 API - `ConcurrentHashMap`: `https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html`

### 5.3 `System.nanoTime()` 기반 반복 측정과 워밍업

- 핵심:
  - 매우 짧은 구간의 시간 비교에는 `System.nanoTime()`이 적합합니다.
  - 첫 호출 편차를 줄이기 위해 워밍업을 먼저 수행한 뒤 실제 측정을 시작합니다.
- 왜 쓰는가:
  - task04는 절대 성능보다 “최적화 전후 상대 차이”를 확인하는 것이 목적입니다.
  - 워밍업 없이 바로 측정하면 첫 호출 초기화 비용이 비교 결과를 왜곡할 수 있습니다.
- 참고 링크:
  - Java SE 25 API - `System.nanoTime()`: `https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/System.html#nanoTime()`

## 6. 실행·검증 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

최적화 전략 조회:

```bash
curl -s "http://localhost:8080/mission06/task04/aop-performance/strategies"
```

최적화 전후 성능 비교:

```bash
curl -s "http://localhost:8080/mission06/task04/aop-performance/compare?iterations=120&payloadSize=480"
```

task04 테스트 실행:

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.AopPerformanceOptimizationControllerTest
```

예상 결과:

- `/strategies`는 적용한 최적화 전략 3개를 반환합니다.
- `/compare`는 기본 AOP와 최적화 AOP의 총 실행 시간, snapshot 횟수, cache hit/miss를 반환합니다.
- 테스트는 AOP 프록시 적용 여부, 최적화 수치 개선, 정상/예외 응답을 모두 통과해야 합니다.

## 7. 결과 확인 방법

- 성공 기준:
  - 기본 버전과 최적화 버전의 비즈니스 결과가 동일해야 합니다.
  - `baselineSnapshotCount`는 반복 횟수와 같아야 합니다.
  - `optimizedSnapshotCount`는 기본 버전보다 작아야 합니다.
  - `optimizedMetadataCacheMissCount`는 첫 호출 1회만 발생해야 합니다.
  - `optimizedElapsedNanos`가 `baselineElapsedNanos`보다 작아야 합니다.

- 응답 스냅샷 파일:
  - `docs/mission-06-spring-core-advanced/task-04-aop-performance-optimization/responses/optimization-strategies.txt`
  - `docs/mission-06-spring-core-advanced/task-04-aop-performance-optimization/responses/performance-compare.txt`

- 테스트 로그 파일:
  - `docs/mission-06-spring-core-advanced/task-04-aop-performance-optimization/task04-gradle-test-output.txt`

실제 비교 응답 요약:

```json
{
  "iterations": 120,
  "payloadSize": 480,
  "baselineElapsedNanos": 8010459,
  "optimizedElapsedNanos": 3409417,
  "baselineSnapshotCount": 120,
  "optimizedSnapshotCount": 0,
  "optimizedMetadataCacheHitCount": 119,
  "optimizedMetadataCacheMissCount": 1,
  "resultEquality": true,
  "improvementPercent": 57.43793208354228
}
```

위 결과에서는 기본 버전 대비 최적화 버전이 약 `57.44%` 빠르게 측정되었습니다.

## 8. 학습 내용

- AOP 성능 문제는 “애스펙트를 썼다” 자체보다, 애스펙트 안에서 무엇을 매번 하느냐에 더 크게 좌우됩니다. 특히 리플렉션 문자열 조합, 무거운 로깅용 포맷팅, 불필요한 객체 생성은 반복 호출 환경에서 곧바로 비용으로 누적됩니다.
- 포인트컷 범위를 좁히면 모든 메서드를 감싸는 것보다 훨씬 예측 가능한 구조가 됩니다. task04에서는 비교 대상 메서드만 명시적으로 지정해, 어떤 로직에 비용이 붙는지 코드를 읽는 사람도 바로 파악할 수 있게 했습니다.
- 캐시는 애플리케이션 전체 성능만이 아니라 AOP 같은 공통 부가 기능에도 유효합니다. 동일 메서드의 메타데이터를 재사용하면, 업무 로직과 무관한 반복 비용을 줄일 수 있습니다.
- “비싼 작업을 아예 없애는 것”만이 최적화는 아닙니다. 느린 호출일 때만 상세 스냅샷을 남기고, 평상시에는 가벼운 경로로 지나가게 하는 식으로 조건부 실행을 두면 관측 가능성과 성능을 함께 가져갈 수 있습니다.
