# 스프링 핵심 원리 - 고급: ThreadLocal을 사용한 데이터베이스 연결 관리

이 문서는 `mission-06-spring-core-advanced`의 `task-07-threadlocal-connection-management` 구현 결과를 정리한 보고서입니다.
`ThreadLocal`을 사용해 같은 스레드 안에서는 하나의 데이터베이스 연결을 재사용하고, 다른 스레드와는 연결을 분리하는 구조를 설계했습니다. 또한 직접 연결 획득 방식과 `ThreadLocal` 재사용 방식의 연결 획득 횟수와 실행 시간을 비교하고, Chrome으로 결과를 캡처했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-06-spring-core-advanced` / `task-07-threadlocal-connection-management`
- 목표:
  - `ThreadLocal`을 사용해 각 스레드에 고유한 DB 연결을 바인딩하는 연결 관리자 클래스를 구현합니다.
  - 같은 스레드 내부의 여러 저장소 호출이 같은 연결을 재사용하는지 검증합니다.
  - 직접 연결 획득 방식과 `ThreadLocal` 재사용 방식의 연결 획득 횟수와 실행 시간을 비교합니다.
  - 테스트, 응답 스냅샷, 브라우저 스크린샷을 함께 남겨 제출 가능한 형태로 정리합니다.
- 엔드포인트:
  - `GET /mission06/task07/thread-local-connections/concepts`
  - `GET /mission06/task07/thread-local-connections/plans/{planId}/demo`
  - `GET /mission06/task07/thread-local-connections/performance`

설계한 시스템 정의:

- 연결 관리자: `ThreadLocalConnectionManager`
- 연결 추적 저장소: `ThreadLocalConnectionAuditStore`
- 직접 연결 계수기: `DirectConnectionMetrics`
- 저장소: `ThreadLocalConnectionDemoRepository`
- 비교 서비스: `ThreadLocalConnectionStudyService`

핵심 동작 규칙:

1. `executeInSession()`이 처음 호출되면 현재 스레드에 새 `Connection`을 열고 `ThreadLocal`에 저장합니다.
2. 같은 스레드 안에서 추가 작업이 이어지면 기존 `Connection`을 다시 사용합니다.
3. 작업이 끝나면 `Connection.close()`와 `ThreadLocal.remove()`를 반드시 수행합니다.
4. 성능 비교에서는 “호출마다 새 연결을 여는 방식”과 “스레드당 하나의 연결을 재사용하는 방식”의 연결 획득 횟수를 비교합니다.

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/controller/ThreadLocalConnectionController.java` | 개념 요약, 연결 재사용 데모, 성능 비교 API 제공 |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/controller/ThreadLocalConnectionExceptionHandler.java` | 잘못된 입력과 내부 상태 오류를 JSON 응답으로 변환 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/dto/ThreadLocalConnectionConceptResponse.java` | ThreadLocal 핵심 개념 요약 응답 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/dto/ThreadLocalConnectionDemoResponse.java` | 연결 재사용 데모 결과 응답 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/dto/ThreadLocalConnectionPerformanceResponse.java` | 연결 획득 횟수와 실행 시간 비교 응답 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/dto/ThreadLocalConnectionErrorResponse.java` | 오류 응답 DTO |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/repository/ThreadLocalConnectionDemoRepository.java` | ThreadLocal 재사용 방식과 직접 연결 획득 방식을 각각 수행 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/service/ThreadLocalConnectionStudyService.java` | 데모 실행과 멀티스레드 성능 비교를 조합 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/service/ThreadLocalConnectionDemoResult.java` | 데모 내부 결과 모델 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/service/ThreadLocalConnectionPerformanceResult.java` | 성능 비교 내부 결과 모델 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalConnectionManager.java` | 스레드별 DB 연결 바인딩, 재사용, 해제를 담당하는 핵심 클래스 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalConnectionAuditStore.java` | 연결 생성/해제 감사 로그 저장 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalConnectionAuditEntry.java` | 감사 로그 한 건의 phase와 메시지 표현 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalConnectionVisit.java` | 저장소 방문 시 어떤 스레드/연결을 썼는지 기록 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalSessionSnapshot.java` | 현재 스레드에 바인딩된 연결 상태 스냅샷 |
| Support | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/DirectConnectionMetrics.java` | 직접 연결 획득 횟수 카운트 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/ThreadLocalConnectionControllerTest.java` | 연결 재사용, 비교 수치, API 응답, 예외 응답 검증 |

## 3. 구현 단계와 주요 코드 해설

1. `ThreadLocalConnectionManager`를 구현해 현재 스레드별로 `Connection`을 보관하도록 만들었습니다.
   - 스레드에 연결이 없으면 새로 열고 `ThreadLocal`에 저장합니다.
   - 이미 연결이 있으면 같은 연결을 다시 사용합니다.
   - 작업 종료 시 `close()`와 `remove()`를 함께 호출해 누수와 잘못된 재사용을 막습니다.

2. 저장소 호출이 실제로 같은 연결을 공유하는지 확인할 수 있도록 `ThreadLocalConnectionVisit`과 `ThreadLocalSessionSnapshot`을 만들었습니다.
   - `loadPlanSummary()`와 `loadApprovalHistory()`가 같은 `connectionId`를 쓰는지 응답 JSON으로 바로 확인할 수 있습니다.
   - `ThreadLocal`의 개념을 단순 설명이 아니라 “연결 ID 재사용”으로 눈에 보이게 만든 부분입니다.

3. 비교 실험은 `ThreadLocalConnectionStudyService.measurePerformance()`에서 수행합니다.
   - 직접 연결 획득 방식은 저장소 메서드마다 `dataSource.getConnection()`을 호출합니다.
   - `ThreadLocal` 방식은 스레드 하나당 한 번만 연결을 열고 반복 작업 내내 재사용합니다.
   - 실행 시간은 환경에 따라 달라질 수 있으므로, 문서와 테스트에서는 연결 획득 횟수 차이를 더 중요한 지표로 봤습니다.

4. 멀티스레드 실행은 `ExecutorService`로 구성했습니다.
   - `workerCount`만큼 고정 스레드 풀을 만들고, 각 스레드가 반복적으로 저장소 메서드를 호출합니다.
   - `ThreadLocal`은 스레드 단위 저장소이므로, 다른 스레드가 같은 연결을 공유하지 않는다는 점이 이 비교 구조에서 자연스럽게 드러납니다.

5. 제출용 산출물은 세 가지로 정리했습니다.
   - 테스트로 재사용/비교 수치 검증
   - 응답 원문 텍스트 파일 저장
   - Chrome 스크린샷 PNG 생성

요청 흐름 요약:

1. 컨트롤러가 데모 또는 성능 비교 요청을 받습니다.
2. 서비스가 `ThreadLocalConnectionManager`를 사용해 스레드별 연결 세션을 시작합니다.
3. 저장소는 현재 스레드에 바인딩된 연결을 사용하거나, 직접 연결을 새로 열어 작업합니다.
4. 서비스는 연결 획득 횟수와 실행 시간을 모아 응답 DTO로 반환합니다.
5. 작업이 끝나면 연결 관리자에서 `Connection.close()`와 `ThreadLocal.remove()`를 수행합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `ThreadLocalConnectionController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/controller/ThreadLocalConnectionController.java`
- 역할: 개념 요약, 연결 재사용 데모, 성능 비교 API 제공
- 상세 설명:
- 기본 경로: `/mission06/task07/thread-local-connections`
- 매핑 메서드:
  - `GET /concepts` -> ThreadLocal 핵심 원리 요약
  - `GET /plans/{planId}/demo` -> 같은 스레드에서 연결 재사용 확인
  - `GET /performance` -> 직접 연결 방식과 ThreadLocal 방식 비교
- 컨트롤러는 요청 파라미터를 서비스로 넘기고, 결과를 응답 DTO로 반환하는 역할만 담당합니다.

<details>
<summary><code>ThreadLocalConnectionController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto.ThreadLocalConnectionConceptResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto.ThreadLocalConnectionDemoResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto.ThreadLocalConnectionPerformanceResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionStudyService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task07/thread-local-connections")
public class ThreadLocalConnectionController {

    private final ThreadLocalConnectionStudyService threadLocalConnectionStudyService;

    public ThreadLocalConnectionController(ThreadLocalConnectionStudyService threadLocalConnectionStudyService) {
        this.threadLocalConnectionStudyService = threadLocalConnectionStudyService;
    }

    @GetMapping("/concepts")
    public ThreadLocalConnectionConceptResponse concepts() {
        return new ThreadLocalConnectionConceptResponse(
                "ThreadLocal은 현재 스레드 전용 저장소를 제공해 같은 요청 흐름 안에서 하나의 DB 연결을 재사용할 수 있게 합니다.",
                List.of(
                        "같은 스레드에서는 같은 Connection을 재사용합니다.",
                        "다른 스레드에서는 서로 다른 Connection이 바인딩됩니다.",
                        "작업이 끝나면 반드시 remove/close로 정리해야 메모리 누수와 잘못된 재사용을 막을 수 있습니다."
                )
        );
    }

    @GetMapping("/plans/{planId}/demo")
    public ThreadLocalConnectionDemoResponse demo(
            @PathVariable String planId,
            @RequestParam(defaultValue = "release-engineer") String operatorId
    ) {
        return ThreadLocalConnectionDemoResponse.from(
                threadLocalConnectionStudyService.demonstrateThreadBoundConnection(planId, operatorId)
        );
    }

    @GetMapping("/performance")
    public ThreadLocalConnectionPerformanceResponse performance(
            @RequestParam(defaultValue = "4") int workerCount,
            @RequestParam(defaultValue = "150") int iterationsPerWorker
    ) {
        return ThreadLocalConnectionPerformanceResponse.from(
                threadLocalConnectionStudyService.measurePerformance(workerCount, iterationsPerWorker)
        );
    }
}
```

</details>

### 4.2 `ThreadLocalConnectionExceptionHandler.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/controller/ThreadLocalConnectionExceptionHandler.java`
- 역할: 잘못된 입력과 내부 상태 오류를 JSON 응답으로 변환
- 상세 설명:
- `workerCount`, `iterationsPerWorker`, `planId`, `operatorId` 검증 실패는 400으로 변환합니다.
- 연결이 없는 상태에서 현재 연결을 읽으려는 경우 같은 내부 상태 오류는 500으로 응답합니다.
- 요청 경로를 함께 반환해 어떤 API에서 오류가 났는지 문서와 브라우저에서 확인하기 쉽게 했습니다.

<details>
<summary><code>ThreadLocalConnectionExceptionHandler.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto.ThreadLocalConnectionErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ThreadLocalConnectionController.class)
public class ThreadLocalConnectionExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ThreadLocalConnectionErrorResponse handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new ThreadLocalConnectionErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IllegalStateException.class)
    public ThreadLocalConnectionErrorResponse handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return new ThreadLocalConnectionErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
```

</details>

### 4.3 `ThreadLocalConnectionConceptResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/dto/ThreadLocalConnectionConceptResponse.java`
- 역할: ThreadLocal 핵심 개념 요약 응답
- 상세 설명:
- 개념 요약 API에서 보여 줄 학습용 텍스트를 담습니다.
- `topic`은 태스크 주제, `principles`는 핵심 원리 3개를 담습니다.
- 문서와 API가 같은 학습 내용을 공유하도록 만든 단순 DTO입니다.

<details>
<summary><code>ThreadLocalConnectionConceptResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto;

import java.util.List;

public class ThreadLocalConnectionConceptResponse {

    private final String topic;
    private final List<String> principles;

    public ThreadLocalConnectionConceptResponse(String topic, List<String> principles) {
        this.topic = topic;
        this.principles = List.copyOf(principles);
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getPrinciples() {
        return principles;
    }
}
```

</details>

### 4.4 `ThreadLocalConnectionDemoResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/dto/ThreadLocalConnectionDemoResponse.java`
- 역할: 연결 재사용 데모 결과 응답
- 상세 설명:
- 같은 스레드에서 어떤 연결 ID를 사용했는지, 저장소 두 번 호출이 같은 연결을 썼는지 응답에 포함합니다.
- `auditTrail`은 연결 생성과 해제 시점을 보여 줍니다.
- `from()` 메서드로 내부 결과 모델을 API 응답 형태로 변환합니다.

<details>
<summary><code>ThreadLocalConnectionDemoResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionDemoResult;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionAuditEntry;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionVisit;
import java.util.List;

public class ThreadLocalConnectionDemoResponse {

    private final String planId;
    private final String operatorId;
    private final String threadName;
    private final int connectionId;
    private final int openedConnectionCount;
    private final String resultMessage;
    private final List<ThreadLocalConnectionVisit> repositoryVisits;
    private final List<ThreadLocalConnectionAuditEntry> auditTrail;

    public ThreadLocalConnectionDemoResponse(
            String planId,
            String operatorId,
            String threadName,
            int connectionId,
            int openedConnectionCount,
            String resultMessage,
            List<ThreadLocalConnectionVisit> repositoryVisits,
            List<ThreadLocalConnectionAuditEntry> auditTrail
    ) {
        this.planId = planId;
        this.operatorId = operatorId;
        this.threadName = threadName;
        this.connectionId = connectionId;
        this.openedConnectionCount = openedConnectionCount;
        this.resultMessage = resultMessage;
        this.repositoryVisits = List.copyOf(repositoryVisits);
        this.auditTrail = List.copyOf(auditTrail);
    }

    public static ThreadLocalConnectionDemoResponse from(ThreadLocalConnectionDemoResult result) {
        return new ThreadLocalConnectionDemoResponse(
                result.getPlanId(),
                result.getOperatorId(),
                result.getThreadName(),
                result.getConnectionId(),
                result.getOpenedConnectionCount(),
                result.getResultMessage(),
                result.getRepositoryVisits(),
                result.getAuditTrail()
        );
    }

    public String getPlanId() {
        return planId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public int getOpenedConnectionCount() {
        return openedConnectionCount;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public List<ThreadLocalConnectionVisit> getRepositoryVisits() {
        return repositoryVisits;
    }

    public List<ThreadLocalConnectionAuditEntry> getAuditTrail() {
        return auditTrail;
    }
}
```

</details>

### 4.5 `ThreadLocalConnectionPerformanceResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/dto/ThreadLocalConnectionPerformanceResponse.java`
- 역할: 연결 획득 횟수와 실행 시간 비교 응답
- 상세 설명:
- 직접 방식과 `ThreadLocal` 방식의 시간/연결 획득 횟수를 함께 반환합니다.
- `reuseSavings`는 두 방식의 연결 획득 횟수 차이를 바로 보여 주는 핵심 필드입니다.
- 스크린샷에서 성능 비교 포인트를 한 번에 읽을 수 있게 만든 DTO입니다.

<details>
<summary><code>ThreadLocalConnectionPerformanceResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionPerformanceResult;
import java.util.List;

public class ThreadLocalConnectionPerformanceResponse {

    private final int workerCount;
    private final int iterationsPerWorker;
    private final long directElapsedMs;
    private final long threadLocalElapsedMs;
    private final int directConnectionAcquisitions;
    private final int threadLocalConnectionAcquisitions;
    private final int reuseSavings;
    private final List<String> notes;

    public ThreadLocalConnectionPerformanceResponse(
            int workerCount,
            int iterationsPerWorker,
            long directElapsedMs,
            long threadLocalElapsedMs,
            int directConnectionAcquisitions,
            int threadLocalConnectionAcquisitions,
            int reuseSavings,
            List<String> notes
    ) {
        this.workerCount = workerCount;
        this.iterationsPerWorker = iterationsPerWorker;
        this.directElapsedMs = directElapsedMs;
        this.threadLocalElapsedMs = threadLocalElapsedMs;
        this.directConnectionAcquisitions = directConnectionAcquisitions;
        this.threadLocalConnectionAcquisitions = threadLocalConnectionAcquisitions;
        this.reuseSavings = reuseSavings;
        this.notes = List.copyOf(notes);
    }

    public static ThreadLocalConnectionPerformanceResponse from(ThreadLocalConnectionPerformanceResult result) {
        return new ThreadLocalConnectionPerformanceResponse(
                result.getWorkerCount(),
                result.getIterationsPerWorker(),
                result.getDirectElapsedMs(),
                result.getThreadLocalElapsedMs(),
                result.getDirectConnectionAcquisitions(),
                result.getThreadLocalConnectionAcquisitions(),
                result.getReuseSavings(),
                result.getNotes()
        );
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public int getIterationsPerWorker() {
        return iterationsPerWorker;
    }

    public long getDirectElapsedMs() {
        return directElapsedMs;
    }

    public long getThreadLocalElapsedMs() {
        return threadLocalElapsedMs;
    }

    public int getDirectConnectionAcquisitions() {
        return directConnectionAcquisitions;
    }

    public int getThreadLocalConnectionAcquisitions() {
        return threadLocalConnectionAcquisitions;
    }

    public int getReuseSavings() {
        return reuseSavings;
    }

    public List<String> getNotes() {
        return notes;
    }
}
```

</details>

### 4.6 `ThreadLocalConnectionErrorResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/dto/ThreadLocalConnectionErrorResponse.java`
- 역할: 오류 응답 DTO
- 상세 설명:
- 상태 코드, 에러명, 메시지, 요청 경로를 묶어 공통 오류 응답 구조를 제공합니다.
- 벤치마크 입력값 오류를 테스트와 브라우저에서 한눈에 확인할 수 있게 해 줍니다.
- 예외 처리 로직과 응답 구조를 분리해 컨트롤러를 단순하게 유지합니다.

<details>
<summary><code>ThreadLocalConnectionErrorResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto;

public class ThreadLocalConnectionErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ThreadLocalConnectionErrorResponse(int status, String error, String message, String path) {
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

### 4.7 `ThreadLocalConnectionDemoRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/repository/ThreadLocalConnectionDemoRepository.java`
- 역할: ThreadLocal 재사용 방식과 직접 연결 획득 방식을 각각 수행
- 상세 설명:
- `loadPlanSummary()`, `loadApprovalHistory()`는 현재 스레드에 바인딩된 연결을 사용합니다.
- `loadPlanSummaryDirect()`, `loadApprovalHistoryDirect()`는 호출마다 `dataSource.getConnection()`으로 새 연결을 엽니다.
- 두 방식을 한 저장소 안에 모아 비교 기준을 동일하게 맞췄습니다.

<details>
<summary><code>ThreadLocalConnectionDemoRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.repository;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.DirectConnectionMetrics;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionManager;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionVisit;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class ThreadLocalConnectionDemoRepository {

    private final ThreadLocalConnectionManager threadLocalConnectionManager;
    private final DataSource dataSource;

    public ThreadLocalConnectionDemoRepository(
            ThreadLocalConnectionManager threadLocalConnectionManager,
            DataSource dataSource
    ) {
        this.threadLocalConnectionManager = threadLocalConnectionManager;
        this.dataSource = dataSource;
    }

    public ThreadLocalConnectionVisit loadPlanSummary(String planId) {
        return threadLocalConnectionManager.withCurrentConnection(connection -> createVisit("loadPlanSummary", connection));
    }

    public ThreadLocalConnectionVisit loadApprovalHistory(String planId) {
        return threadLocalConnectionManager.withCurrentConnection(connection -> createVisit("loadApprovalHistory", connection));
    }

    public String loadPlanSummaryDirect(String planId, DirectConnectionMetrics directConnectionMetrics) {
        directConnectionMetrics.recordOpen();
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName() + ":" + planId;
        } catch (Exception exception) {
            throw new IllegalStateException("직접 연결 plan summary 조회에 실패했습니다.", exception);
        }
    }

    public String loadApprovalHistoryDirect(String planId, DirectConnectionMetrics directConnectionMetrics) {
        directConnectionMetrics.recordOpen();
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName() + ":" + planId;
        } catch (Exception exception) {
            throw new IllegalStateException("직접 연결 approval history 조회에 실패했습니다.", exception);
        }
    }

    private ThreadLocalConnectionVisit createVisit(String repositoryMethod, Connection connection) throws SQLException {
        return new ThreadLocalConnectionVisit(
                repositoryMethod,
                Thread.currentThread().getName(),
                threadLocalConnectionManager.currentSnapshot().getConnectionId(),
                connection.getMetaData().getDatabaseProductName()
        );
    }
}
```

</details>

### 4.8 `ThreadLocalConnectionStudyService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/service/ThreadLocalConnectionStudyService.java`
- 역할: 데모 실행과 멀티스레드 성능 비교를 조합
- 상세 설명:
- 핵심 공개 메서드:
  - `demonstrateThreadBoundConnection()` -> 같은 스레드에서 연결 재사용을 검증
  - `measurePerformance()` -> 직접 연결 방식과 ThreadLocal 방식 비교
- `ExecutorService`로 여러 스레드를 띄워 스레드별 연결 분리와 전체 연결 획득 횟수 차이를 동시에 보여 줍니다.

<details>
<summary><code>ThreadLocalConnectionStudyService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.repository.ThreadLocalConnectionDemoRepository;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.DirectConnectionMetrics;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionAuditStore;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionManager;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionVisit;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalSessionSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.stereotype.Service;

@Service
public class ThreadLocalConnectionStudyService {

    private final ThreadLocalConnectionManager threadLocalConnectionManager;
    private final ThreadLocalConnectionAuditStore threadLocalConnectionAuditStore;
    private final ThreadLocalConnectionDemoRepository threadLocalConnectionDemoRepository;
    private final DirectConnectionMetrics directConnectionMetrics;

    public ThreadLocalConnectionStudyService(
            ThreadLocalConnectionManager threadLocalConnectionManager,
            ThreadLocalConnectionAuditStore threadLocalConnectionAuditStore,
            ThreadLocalConnectionDemoRepository threadLocalConnectionDemoRepository,
            DirectConnectionMetrics directConnectionMetrics
    ) {
        this.threadLocalConnectionManager = threadLocalConnectionManager;
        this.threadLocalConnectionAuditStore = threadLocalConnectionAuditStore;
        this.threadLocalConnectionDemoRepository = threadLocalConnectionDemoRepository;
        this.directConnectionMetrics = directConnectionMetrics;
    }

    public ThreadLocalConnectionDemoResult demonstrateThreadBoundConnection(String planId, String operatorId) {
        validatePlanId(planId);
        validateOperatorId(operatorId);

        threadLocalConnectionAuditStore.reset();
        threadLocalConnectionManager.resetOpenedConnectionCount();

        DemoCapture capture = threadLocalConnectionManager.executeInSession("deployment-plan-demo", connection -> {
            List<ThreadLocalConnectionVisit> visits = new ArrayList<>();
            visits.add(threadLocalConnectionDemoRepository.loadPlanSummary(planId));
            visits.add(threadLocalConnectionDemoRepository.loadApprovalHistory(planId));

            ThreadLocalSessionSnapshot snapshot = threadLocalConnectionManager.currentSnapshot();

            return new DemoCapture(snapshot.getThreadName(), snapshot.getConnectionId(), visits);
        });

        return new ThreadLocalConnectionDemoResult(
                planId,
                operatorId,
                capture.threadName(),
                capture.connectionId(),
                threadLocalConnectionManager.getOpenedConnectionCount(),
                "같은 스레드에서 plan summary와 approval history 조회가 같은 DB 연결을 재사용했습니다.",
                capture.repositoryVisits(),
                threadLocalConnectionAuditStore.getEntries()
        );
    }

    public ThreadLocalConnectionPerformanceResult measurePerformance(int workerCount, int iterationsPerWorker) {
        validateBenchmarkInput(workerCount, iterationsPerWorker);

        BenchmarkOutcome directOutcome = benchmarkDirect(workerCount, iterationsPerWorker);
        BenchmarkOutcome threadLocalOutcome = benchmarkThreadLocal(workerCount, iterationsPerWorker);

        return new ThreadLocalConnectionPerformanceResult(
                workerCount,
                iterationsPerWorker,
                directOutcome.elapsedMs(),
                threadLocalOutcome.elapsedMs(),
                directOutcome.connectionAcquisitions(),
                threadLocalOutcome.connectionAcquisitions(),
                directOutcome.connectionAcquisitions() - threadLocalOutcome.connectionAcquisitions(),
                List.of(
                        "직접 획득 방식은 repository 호출마다 새 Connection을 열고 닫습니다.",
                        "ThreadLocal 방식은 같은 스레드 안에서 하나의 Connection을 재사용합니다.",
                        "실행 시간은 환경에 따라 달라질 수 있지만, 연결 획득 횟수 차이는 안정적으로 확인할 수 있습니다."
                )
        );
    }

    private BenchmarkOutcome benchmarkDirect(int workerCount, int iterationsPerWorker) {
        directConnectionMetrics.reset();
        ExecutorService executorService = Executors.newFixedThreadPool(workerCount);
        long start = System.nanoTime();

        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (int workerIndex = 0; workerIndex < workerCount; workerIndex++) {
                final int workerId = workerIndex;
                futures.add(executorService.submit(runDirectScenario(workerId, iterationsPerWorker)));
            }
            waitForAll(futures);
        } finally {
            executorService.shutdownNow();
        }

        return new BenchmarkOutcome((System.nanoTime() - start) / 1_000_000, directConnectionMetrics.getOpenedConnectionCount());
    }

    private BenchmarkOutcome benchmarkThreadLocal(int workerCount, int iterationsPerWorker) {
        threadLocalConnectionManager.resetOpenedConnectionCount();
        ExecutorService executorService = Executors.newFixedThreadPool(workerCount);
        long start = System.nanoTime();

        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (int workerIndex = 0; workerIndex < workerCount; workerIndex++) {
                final int workerId = workerIndex;
                futures.add(executorService.submit(runThreadLocalScenario(workerId, iterationsPerWorker)));
            }
            waitForAll(futures);
        } finally {
            executorService.shutdownNow();
        }

        return new BenchmarkOutcome((System.nanoTime() - start) / 1_000_000, threadLocalConnectionManager.getOpenedConnectionCount());
    }

    private Callable<Void> runDirectScenario(int workerId, int iterationsPerWorker) {
        return () -> {
            for (int iteration = 0; iteration < iterationsPerWorker; iteration++) {
                String planId = "direct-plan-" + workerId + "-" + iteration;
                threadLocalConnectionDemoRepository.loadPlanSummaryDirect(planId, directConnectionMetrics);
                threadLocalConnectionDemoRepository.loadApprovalHistoryDirect(planId, directConnectionMetrics);
            }
            return null;
        };
    }

    private Callable<Void> runThreadLocalScenario(int workerId, int iterationsPerWorker) {
        return () -> {
            threadLocalConnectionManager.executeInSession("worker-" + workerId, connection -> {
                for (int iteration = 0; iteration < iterationsPerWorker; iteration++) {
                    String planId = "threadlocal-plan-" + workerId + "-" + iteration;
                    threadLocalConnectionDemoRepository.loadPlanSummary(planId);
                    threadLocalConnectionDemoRepository.loadApprovalHistory(planId);
                }
                return null;
            });
            return null;
        };
    }

    private void waitForAll(List<Future<Void>> futures) {
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("성능 측정 스레드가 인터럽트되었습니다.", exception);
            } catch (ExecutionException exception) {
                throw new IllegalStateException("성능 측정 작업이 실패했습니다.", exception.getCause());
            }
        }
    }

    private void validatePlanId(String planId) {
        if (planId == null || planId.isBlank()) {
            throw new IllegalArgumentException("planId는 비어 있을 수 없습니다.");
        }
    }

    private void validateOperatorId(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            throw new IllegalArgumentException("operatorId는 비어 있을 수 없습니다.");
        }
    }

    private void validateBenchmarkInput(int workerCount, int iterationsPerWorker) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount는 1 이상이어야 합니다.");
        }
        if (iterationsPerWorker <= 0) {
            throw new IllegalArgumentException("iterationsPerWorker는 1 이상이어야 합니다.");
        }
    }

    private record BenchmarkOutcome(long elapsedMs, int connectionAcquisitions) {
    }

    private record DemoCapture(
            String threadName,
            int connectionId,
            List<ThreadLocalConnectionVisit> repositoryVisits
    ) {
    }
}
```

</details>

### 4.9 `ThreadLocalConnectionDemoResult.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/service/ThreadLocalConnectionDemoResult.java`
- 역할: 데모 내부 결과 모델
- 상세 설명:
- 데모 시나리오에서 생성한 연결 ID, 스레드명, 저장소 방문 기록, 감사 로그를 하나로 묶습니다.
- 컨트롤러는 이 모델을 그대로 DTO 변환에 사용합니다.
- 같은 스레드 연결 재사용 결과를 서비스 단계에서 명확히 보관하는 용도입니다.

<details>
<summary><code>ThreadLocalConnectionDemoResult.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionAuditEntry;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionVisit;
import java.util.List;

public class ThreadLocalConnectionDemoResult {

    private final String planId;
    private final String operatorId;
    private final String threadName;
    private final int connectionId;
    private final int openedConnectionCount;
    private final String resultMessage;
    private final List<ThreadLocalConnectionVisit> repositoryVisits;
    private final List<ThreadLocalConnectionAuditEntry> auditTrail;

    public ThreadLocalConnectionDemoResult(
            String planId,
            String operatorId,
            String threadName,
            int connectionId,
            int openedConnectionCount,
            String resultMessage,
            List<ThreadLocalConnectionVisit> repositoryVisits,
            List<ThreadLocalConnectionAuditEntry> auditTrail
    ) {
        this.planId = planId;
        this.operatorId = operatorId;
        this.threadName = threadName;
        this.connectionId = connectionId;
        this.openedConnectionCount = openedConnectionCount;
        this.resultMessage = resultMessage;
        this.repositoryVisits = List.copyOf(repositoryVisits);
        this.auditTrail = List.copyOf(auditTrail);
    }

    public String getPlanId() {
        return planId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public int getOpenedConnectionCount() {
        return openedConnectionCount;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public List<ThreadLocalConnectionVisit> getRepositoryVisits() {
        return repositoryVisits;
    }

    public List<ThreadLocalConnectionAuditEntry> getAuditTrail() {
        return auditTrail;
    }
}
```

</details>

### 4.10 `ThreadLocalConnectionPerformanceResult.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/service/ThreadLocalConnectionPerformanceResult.java`
- 역할: 성능 비교 내부 결과 모델
- 상세 설명:
- 연결 획득 횟수, 실행 시간, 절감 수치, 설명 문구를 묶습니다.
- 테스트에서는 이 모델의 연결 획득 횟수를 직접 검증합니다.
- 환경 차이가 있는 시간 값과, 비교적 안정적인 연결 획득 횟수를 함께 보관합니다.

<details>
<summary><code>ThreadLocalConnectionPerformanceResult.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service;

import java.util.List;

public class ThreadLocalConnectionPerformanceResult {

    private final int workerCount;
    private final int iterationsPerWorker;
    private final long directElapsedMs;
    private final long threadLocalElapsedMs;
    private final int directConnectionAcquisitions;
    private final int threadLocalConnectionAcquisitions;
    private final int reuseSavings;
    private final List<String> notes;

    public ThreadLocalConnectionPerformanceResult(
            int workerCount,
            int iterationsPerWorker,
            long directElapsedMs,
            long threadLocalElapsedMs,
            int directConnectionAcquisitions,
            int threadLocalConnectionAcquisitions,
            int reuseSavings,
            List<String> notes
    ) {
        this.workerCount = workerCount;
        this.iterationsPerWorker = iterationsPerWorker;
        this.directElapsedMs = directElapsedMs;
        this.threadLocalElapsedMs = threadLocalElapsedMs;
        this.directConnectionAcquisitions = directConnectionAcquisitions;
        this.threadLocalConnectionAcquisitions = threadLocalConnectionAcquisitions;
        this.reuseSavings = reuseSavings;
        this.notes = List.copyOf(notes);
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public int getIterationsPerWorker() {
        return iterationsPerWorker;
    }

    public long getDirectElapsedMs() {
        return directElapsedMs;
    }

    public long getThreadLocalElapsedMs() {
        return threadLocalElapsedMs;
    }

    public int getDirectConnectionAcquisitions() {
        return directConnectionAcquisitions;
    }

    public int getThreadLocalConnectionAcquisitions() {
        return threadLocalConnectionAcquisitions;
    }

    public int getReuseSavings() {
        return reuseSavings;
    }

    public List<String> getNotes() {
        return notes;
    }
}
```

</details>

### 4.11 `ThreadLocalConnectionManager.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalConnectionManager.java`
- 역할: 스레드별 DB 연결 바인딩, 재사용, 해제를 담당하는 핵심 클래스
- 상세 설명:
- `executeInSession()`은 바깥 세션이면 새 연결을 열고, 이미 세션이 있으면 같은 연결을 재사용합니다.
- `withCurrentConnection()`은 현재 스레드에 이미 바인딩된 연결을 사용하는 저장소 호출용 진입점입니다.
- `closeConnection()`에서 `Connection.close()`와 `threadBoundConnection.remove()`를 함께 수행하는 점이 핵심입니다.

<details>
<summary><code>ThreadLocalConnectionManager.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class ThreadLocalConnectionManager {

    private final DataSource dataSource;
    private final ThreadLocalConnectionAuditStore auditStore;
    private final ThreadLocal<ManagedConnectionContext> threadBoundConnection = new ThreadLocal<>();
    private final AtomicInteger connectionSequence = new AtomicInteger(1);
    private final AtomicInteger openedConnectionCount = new AtomicInteger();

    public ThreadLocalConnectionManager(DataSource dataSource, ThreadLocalConnectionAuditStore auditStore) {
        this.dataSource = dataSource;
        this.auditStore = auditStore;
    }

    public void resetOpenedConnectionCount() {
        openedConnectionCount.set(0);
    }

    public int getOpenedConnectionCount() {
        return openedConnectionCount.get();
    }

    public <T> T executeInSession(String sessionLabel, ConnectionCallback<T> callback) {
        ManagedConnectionContext existingContext = threadBoundConnection.get();
        if (existingContext != null) {
            existingContext.incrementNestingLevel();
            auditStore.add(
                    "REUSE",
                    "기존 연결 재사용 - thread=" + existingContext.threadName
                            + ", connectionId=" + existingContext.connectionId
                            + ", nestingLevel=" + existingContext.nestingLevel
            );
            try {
                return callback.doInConnection(existingContext.connection);
            } catch (SQLException exception) {
                throw new IllegalStateException("ThreadLocal 연결 재사용 작업에 실패했습니다.", exception);
            } finally {
                existingContext.decrementNestingLevel();
            }
        }

        Connection connection = openConnection();
        ManagedConnectionContext newContext = new ManagedConnectionContext(
                connectionSequence.getAndIncrement(),
                connection,
                Thread.currentThread().getName(),
                sessionLabel
        );
        threadBoundConnection.set(newContext);
        openedConnectionCount.incrementAndGet();
        auditStore.add(
                "ACQUIRE",
                "새 연결 생성 - thread=" + newContext.threadName
                        + ", connectionId=" + newContext.connectionId
                        + ", sessionLabel=" + sessionLabel
        );

        try {
            return callback.doInConnection(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("ThreadLocal 세션 작업에 실패했습니다.", exception);
        } finally {
            closeConnection(newContext);
        }
    }

    public <T> T withCurrentConnection(ConnectionCallback<T> callback) {
        ManagedConnectionContext context = requireContext();
        try {
            return callback.doInConnection(context.connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("현재 ThreadLocal 연결 작업에 실패했습니다.", exception);
        }
    }

    public ThreadLocalSessionSnapshot currentSnapshot() {
        ManagedConnectionContext context = requireContext();
        return new ThreadLocalSessionSnapshot(
                context.connectionId,
                context.threadName,
                context.nestingLevel,
                context.sessionLabel
        );
    }

    private ManagedConnectionContext requireContext() {
        ManagedConnectionContext context = threadBoundConnection.get();
        if (context == null) {
            throw new IllegalStateException("현재 스레드에 바인딩된 DB 연결이 없습니다.");
        }
        return context;
    }

    private Connection openConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException exception) {
            throw new IllegalStateException("DB 연결 획득에 실패했습니다.", exception);
        }
    }

    private void closeConnection(ManagedConnectionContext context) {
        try {
            context.connection.close();
            auditStore.add(
                    "RELEASE",
                    "연결 해제 - thread=" + context.threadName
                            + ", connectionId=" + context.connectionId
            );
        } catch (SQLException exception) {
            throw new IllegalStateException("DB 연결 해제에 실패했습니다.", exception);
        } finally {
            threadBoundConnection.remove();
        }
    }

    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T doInConnection(Connection connection) throws SQLException;
    }

    private static final class ManagedConnectionContext {

        private final int connectionId;
        private final Connection connection;
        private final String threadName;
        private final String sessionLabel;
        private int nestingLevel;

        private ManagedConnectionContext(
                int connectionId,
                Connection connection,
                String threadName,
                String sessionLabel
        ) {
            this.connectionId = connectionId;
            this.connection = connection;
            this.threadName = threadName;
            this.sessionLabel = sessionLabel;
            this.nestingLevel = 1;
        }

        private void incrementNestingLevel() {
            nestingLevel++;
        }

        private void decrementNestingLevel() {
            nestingLevel--;
        }
    }
}
```

</details>

### 4.12 `ThreadLocalConnectionAuditStore.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalConnectionAuditStore.java`
- 역할: 연결 생성/해제 감사 로그 저장
- 상세 설명:
- 연결 생성(`ACQUIRE`)과 해제(`RELEASE`) 로그를 메모리에 보관합니다.
- `reset()`으로 데모 시작 전에 이전 로그를 비웁니다.
- ThreadLocal 동작을 응답 JSON으로 다시 보여 주기 위한 보조 저장소입니다.

<details>
<summary><code>ThreadLocalConnectionAuditStore.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ThreadLocalConnectionAuditStore {

    private final List<ThreadLocalConnectionAuditEntry> entries = new ArrayList<>();

    public synchronized void reset() {
        entries.clear();
    }

    public synchronized void add(String phase, String message) {
        entries.add(new ThreadLocalConnectionAuditEntry(phase, message));
    }

    public synchronized List<ThreadLocalConnectionAuditEntry> getEntries() {
        return List.copyOf(entries);
    }
}
```

</details>

### 4.13 `ThreadLocalConnectionAuditEntry.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalConnectionAuditEntry.java`
- 역할: 감사 로그 한 건의 phase와 메시지 표현
- 상세 설명:
- 로그 단계를 `phase`로 분리해 스크린샷과 API 응답에서 읽기 쉽게 만들었습니다.
- 현재 태스크에서는 `ACQUIRE`, `RELEASE`, 그리고 중첩 호출 시 `REUSE`를 표현할 수 있습니다.
- 구조를 단순하게 유지해 테스트에서도 바로 비교할 수 있습니다.

<details>
<summary><code>ThreadLocalConnectionAuditEntry.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

public class ThreadLocalConnectionAuditEntry {

    private final String phase;
    private final String message;

    public ThreadLocalConnectionAuditEntry(String phase, String message) {
        this.phase = phase;
        this.message = message;
    }

    public String getPhase() {
        return phase;
    }

    public String getMessage() {
        return message;
    }
}
```

</details>

### 4.14 `ThreadLocalConnectionVisit.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalConnectionVisit.java`
- 역할: 저장소 방문 시 어떤 스레드/연결을 썼는지 기록
- 상세 설명:
- 저장소 메서드명, 스레드명, 연결 ID, DB 제품명을 함께 보관합니다.
- 같은 스레드에서 두 번 호출했을 때 `connectionId`가 같은지 비교하는 핵심 자료입니다.
- 이 모델 덕분에 “같은 스레드 = 같은 연결”을 응답 본문으로 설명할 수 있습니다.

<details>
<summary><code>ThreadLocalConnectionVisit.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

public class ThreadLocalConnectionVisit {

    private final String repositoryMethod;
    private final String threadName;
    private final int connectionId;
    private final String databaseProduct;

    public ThreadLocalConnectionVisit(
            String repositoryMethod,
            String threadName,
            int connectionId,
            String databaseProduct
    ) {
        this.repositoryMethod = repositoryMethod;
        this.threadName = threadName;
        this.connectionId = connectionId;
        this.databaseProduct = databaseProduct;
    }

    public String getRepositoryMethod() {
        return repositoryMethod;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public String getDatabaseProduct() {
        return databaseProduct;
    }
}
```

</details>

### 4.15 `ThreadLocalSessionSnapshot.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/ThreadLocalSessionSnapshot.java`
- 역할: 현재 스레드에 바인딩된 연결 상태 스냅샷
- 상세 설명:
- 현재 스레드에 어떤 연결 ID가 바인딩돼 있는지, 중첩 깊이는 몇 단계인지 읽을 수 있습니다.
- 저장소 자체가 아닌 연결 관리자 상태를 외부에서 관찰할 수 있게 만든 읽기 모델입니다.
- 데모 응답에서 연결 ID를 안정적으로 추출하는 데 사용합니다.

<details>
<summary><code>ThreadLocalSessionSnapshot.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

public class ThreadLocalSessionSnapshot {

    private final int connectionId;
    private final String threadName;
    private final int nestingLevel;
    private final String sessionLabel;

    public ThreadLocalSessionSnapshot(int connectionId, String threadName, int nestingLevel, String sessionLabel) {
        this.connectionId = connectionId;
        this.threadName = threadName;
        this.nestingLevel = nestingLevel;
        this.sessionLabel = sessionLabel;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getNestingLevel() {
        return nestingLevel;
    }

    public String getSessionLabel() {
        return sessionLabel;
    }
}
```

</details>

### 4.16 `DirectConnectionMetrics.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/support/DirectConnectionMetrics.java`
- 역할: 직접 연결 획득 횟수 카운트
- 상세 설명:
- 직접 연결 획득 방식에서 `dataSource.getConnection()`이 몇 번 호출됐는지 셉니다.
- `AtomicInteger`를 사용해 여러 스레드에서 동시에 증가해도 안전하게 유지합니다.
- 성능 비교 결과에서 `directConnectionAcquisitions` 값을 만드는 핵심 보조 클래스입니다.

<details>
<summary><code>DirectConnectionMetrics.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class DirectConnectionMetrics {

    private final AtomicInteger openedConnectionCount = new AtomicInteger();

    public void reset() {
        openedConnectionCount.set(0);
    }

    public void recordOpen() {
        openedConnectionCount.incrementAndGet();
    }

    public int getOpenedConnectionCount() {
        return openedConnectionCount.get();
    }
}
```

</details>

### 4.17 `ThreadLocalConnectionControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task07_threadlocal_connection_management/ThreadLocalConnectionControllerTest.java`
- 역할: 연결 재사용, 비교 수치, API 응답, 예외 응답 검증
- 상세 설명:
- 검증 시나리오:
  - `demonstrateThreadBoundConnection_reusesSingleConnectionWithinSameThread()` -> 같은 스레드에서 연결 재사용 보장
  - `measurePerformance_usesFewerConnectionsWithThreadLocal()` -> 비교 수치 검증
  - `demoEndpoint_returnsSharedConnectionTrace()` -> 데모 API 응답 구조 검증
  - `performanceEndpoint_returnsConnectionSavings()` -> 성능 API 응답 구조 검증
  - `performanceEndpoint_withInvalidWorkerCount_returns400()` -> 잘못된 입력 시 400 보장
- 성능 테스트는 실행 시간 자체보다 연결 획득 횟수 차이를 기준으로 검증합니다.

<details>
<summary><code>ThreadLocalConnectionControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionDemoResult;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionPerformanceResult;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionStudyService;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ThreadLocalConnectionControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ThreadLocalConnectionStudyService threadLocalConnectionStudyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void demonstrateThreadBoundConnection_reusesSingleConnectionWithinSameThread() {
        ThreadLocalConnectionDemoResult result =
                threadLocalConnectionStudyService.demonstrateThreadBoundConnection("plan-blue", "release-engineer");

        assertThat(result.getOpenedConnectionCount()).isEqualTo(1);
        assertThat(result.getRepositoryVisits()).hasSize(2);
        assertThat(result.getRepositoryVisits().stream()
                .map(visit -> visit.getConnectionId())
                .collect(Collectors.toSet())).hasSize(1);
        assertThat(result.getAuditTrail()).extracting("phase")
                .containsExactly("ACQUIRE", "RELEASE");
    }

    @Test
    void measurePerformance_usesFewerConnectionsWithThreadLocal() {
        ThreadLocalConnectionPerformanceResult result =
                threadLocalConnectionStudyService.measurePerformance(3, 20);

        assertThat(result.getDirectConnectionAcquisitions()).isEqualTo(120);
        assertThat(result.getThreadLocalConnectionAcquisitions()).isEqualTo(3);
        assertThat(result.getReuseSavings()).isEqualTo(117);
    }

    @Test
    void demoEndpoint_returnsSharedConnectionTrace() throws Exception {
        mockMvc.perform(get("/mission06/task07/thread-local-connections/plans/plan-blue/demo")
                        .param("operatorId", "release-engineer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value("plan-blue"))
                .andExpect(jsonPath("$.openedConnectionCount").value(1))
                .andExpect(jsonPath("$.repositoryVisits", hasSize(2)))
                .andExpect(jsonPath("$.repositoryVisits[0].connectionId").isNumber())
                .andExpect(jsonPath("$.repositoryVisits[0].threadName").exists())
                .andExpect(jsonPath("$.repositoryVisits[0].databaseProduct").value("H2"))
                .andExpect(jsonPath("$.auditTrail", hasSize(2)))
                .andExpect(jsonPath("$.auditTrail[0].phase").value("ACQUIRE"))
                .andExpect(jsonPath("$.auditTrail[1].phase").value("RELEASE"));
    }

    @Test
    void performanceEndpoint_returnsConnectionSavings() throws Exception {
        mockMvc.perform(get("/mission06/task07/thread-local-connections/performance")
                        .param("workerCount", "4")
                        .param("iterationsPerWorker", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerCount").value(4))
                .andExpect(jsonPath("$.iterationsPerWorker").value(30))
                .andExpect(jsonPath("$.directConnectionAcquisitions").value(240))
                .andExpect(jsonPath("$.threadLocalConnectionAcquisitions").value(4))
                .andExpect(jsonPath("$.reuseSavings").value(236))
                .andExpect(jsonPath("$.notes", hasSize(3)));
    }

    @Test
    void performanceEndpoint_withInvalidWorkerCount_returns400() throws Exception {
        mockMvc.perform(get("/mission06/task07/thread-local-connections/performance")
                        .param("workerCount", "0")
                        .param("iterationsPerWorker", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("workerCount는 1 이상이어야 합니다."));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- `ThreadLocal`
  - 핵심: 현재 스레드마다 독립된 값을 저장하는 저장소입니다.
  - 왜 쓰는가: 같은 요청 스레드 안에서 하나의 DB 연결, 사용자 컨텍스트, 트랜잭션 상태를 안전하게 재사용할 수 있기 때문입니다.
  - 참고 링크:
    - https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ThreadLocal.html
    - https://docs.spring.io/spring-framework/reference/data-access/transaction/tx-resource-synchronization.html

- 스레드 바운드 리소스 관리
  - 핵심: 특정 리소스(`Connection` 등)를 현재 스레드에 묶어 같은 흐름에서 반복 재사용하는 방식입니다.
  - 왜 쓰는가: 메서드마다 새 연결을 여는 비용을 줄이고, 한 요청 흐름에서 같은 자원을 일관되게 사용하게 만들 수 있기 때문입니다.
  - 참고 링크:
    - https://docs.spring.io/spring-framework/reference/data-access/transaction/strategies.html
    - https://docs.spring.io/spring-framework/reference/data-access/transaction/tx-resource-synchronization.html

- `remove()`와 자원 정리
  - 핵심: `ThreadLocal` 값은 작업이 끝나면 명시적으로 제거해야 합니다.
  - 왜 쓰는가: 스레드 풀 환경에서 이전 요청의 값이 남아 다음 요청으로 섞이거나 메모리 누수가 생기는 문제를 막기 위해서입니다.
  - 참고 링크:
    - https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ThreadLocal.html#remove()

- `ExecutorService`
  - 핵심: 여러 작업을 여러 스레드에서 실행하기 위한 표준 스레드 풀 API입니다.
  - 왜 쓰는가: 여러 스레드가 각자 고유 연결을 받는지 검증하려면 멀티스레드 실행 환경이 필요하기 때문입니다.
  - 참고 링크:
    - https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ExecutorService.html

## 6. 실행·검증 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

ThreadLocal 개념 요약 확인:

```bash
curl -s "http://localhost:8080/mission06/task07/thread-local-connections/concepts"
```

같은 스레드 연결 재사용 데모:

```bash
curl -s "http://localhost:8080/mission06/task07/thread-local-connections/plans/plan-blue/demo?operatorId=release-engineer"
```

성능 비교:

```bash
curl -s "http://localhost:8080/mission06/task07/thread-local-connections/performance?workerCount=4&iterationsPerWorker=120"
```

task07 테스트 실행:

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.ThreadLocalConnectionControllerTest
```

예상 결과:

- `/concepts`는 ThreadLocal의 핵심 원리 3개를 반환합니다.
- `/demo`는 저장소 두 번 호출이 같은 `connectionId`를 사용했다는 결과와 `ACQUIRE/RELEASE` 로그를 반환합니다.
- `/performance`는 직접 연결 방식보다 ThreadLocal 방식의 연결 획득 횟수가 훨씬 적게 나와야 합니다.
- 테스트는 같은 스레드 연결 재사용, 멀티스레드 연결 획득 수 비교, 예외 응답을 모두 통과해야 합니다.

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
  - `/demo` 응답에서 `openedConnectionCount=1`이어야 합니다.
  - `/demo`의 `repositoryVisits[0].connectionId`와 `repositoryVisits[1].connectionId`가 동일해야 합니다.
  - `/performance`에서 `threadLocalConnectionAcquisitions`가 `workerCount`와 같아야 합니다.
  - `/performance`에서 `directConnectionAcquisitions`가 `workerCount * iterationsPerWorker * 2` 값과 같아야 합니다.
  - `reuseSavings`는 양수여야 합니다.

- 응답 스냅샷 파일:
  - `docs/mission-06-spring-core-advanced/task-07-threadlocal-connection-management/responses/concepts-response.txt`
  - `docs/mission-06-spring-core-advanced/task-07-threadlocal-connection-management/responses/demo-response.txt`
  - `docs/mission-06-spring-core-advanced/task-07-threadlocal-connection-management/responses/performance-response.txt`

- 테스트 로그 파일:
  - `docs/mission-06-spring-core-advanced/task-07-threadlocal-connection-management/task07-gradle-test-output.txt`

- Chrome 스크린샷 파일:
  - `docs/mission-06-spring-core-advanced/task-07-threadlocal-connection-management/screenshots/concepts-response.png`
  - `docs/mission-06-spring-core-advanced/task-07-threadlocal-connection-management/screenshots/demo-response.png`
  - `docs/mission-06-spring-core-advanced/task-07-threadlocal-connection-management/screenshots/performance-response.png`

스크린샷 미리보기:

![concepts-response](screenshots/concepts-response.png)
![demo-response](screenshots/demo-response.png)
![performance-response](screenshots/performance-response.png)

실제 측정 응답 요약:

```json
{
  "workerCount": 4,
  "iterationsPerWorker": 120,
  "directElapsedMs": 2,
  "threadLocalElapsedMs": 1,
  "directConnectionAcquisitions": 960,
  "threadLocalConnectionAcquisitions": 4,
  "reuseSavings": 956
}
```

이번 실행에서는 ThreadLocal 방식이 스레드당 한 번만 연결을 열어서 전체 연결 획득 횟수를 `960 -> 4`로 줄였습니다. 실행 시간은 환경에 따라 달라질 수 있으므로, 제출 시에는 스크린샷과 함께 연결 획득 횟수 차이를 주요 근거로 보는 편이 안전합니다.

## 8. 학습 내용

- `ThreadLocal`은 “전역 변수처럼 접근하지만 실제 값은 스레드마다 다르다”는 점이 핵심입니다. 그래서 메서드 파라미터로 매번 연결을 전달하지 않아도, 같은 요청 스레드라면 같은 `Connection`을 참조할 수 있습니다.
- 하지만 `ThreadLocal`은 자동 정리되지 않습니다. 특히 스레드 풀을 쓰는 서버에서는 이전 요청의 값이 남아 다음 요청에서 잘못 재사용될 수 있으므로, 작업 종료 시 `remove()`가 필수입니다.
- 성능 비교에서 더 중요한 것은 절대 시간보다 연결 획득 횟수입니다. 짧은 로컬 실행에서는 시간 차이가 작게 나올 수 있지만, 연결을 수백 번 덜 여는 구조적 차이는 일관되게 확인할 수 있습니다.
- 이 태스크는 스프링의 트랜잭션/리소스 동기화가 왜 스레드 바운드 개념을 많이 사용하는지 이해하는 출발점이 됩니다. 같은 요청 흐름 안에서 동일한 자원을 묶어 관리하면, 코드 구조와 자원 일관성이 함께 좋아집니다.
