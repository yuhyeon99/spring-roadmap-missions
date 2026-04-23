# 스프링 핵심 원리 - 고급: 프록시 패턴을 활용한 보안 검증 시스템 구축해보기

이 문서는 `mission-06-spring-core-advanced`의 `task-01-proxy-security`를 기준으로 정리한 보고서입니다.
프록시 패턴을 사용해 메서드 실행 전후에 인증, 권한 검사, 사후 감사 로그를 넣는 구조를 직접 구현하고 검증했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-06-spring-core-advanced` / `task-01-proxy-security`
- 목표:
  - 동일한 서비스 인터페이스를 구현하는 `실제 서비스`와 `프록시 서비스`를 분리한다.
  - 프록시가 메서드 실행 전에 인증과 권한을 검사하고, 실행 후에는 보안 로그를 남기도록 만든다.
  - 조회와 민감 작업에 서로 다른 권한 정책을 부여해 메서드별 보안 규칙 차이를 확인한다.
- 엔드포인트:
  - `GET /mission06/task01/proxy-security/projects/{projectId}`
  - `POST /mission06/task01/proxy-security/projects/{projectId}/rotate-secrets`

설계한 시스템 정의:

- 보호 대상 인터페이스: `ProtectedProjectService`
- 실제 서비스(Real Subject): `ProtectedProjectServiceImpl`
- 프록시(Proxy): `SecurityVerificationProjectServiceProxy`
- 보안 검증기: `ProjectSecurityVerifier`
- 요청 컨텍스트: `SecurityRequestContext`
- 정책 정의: `ProjectSecurityAction`
  - `VIEW_PROJECT` -> `USER` 이상
  - `ROTATE_SECRETS` -> `ADMIN` 이상

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/controller/ProxySecurityController.java` | 프로젝트 조회/시크릿 재발급 API 진입점 |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/controller/ProxySecurityExceptionHandler.java` | 401/403/400 오류를 JSON 응답으로 변환 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/domain/AccessRole.java` | 역할 계층과 문자열 파싱 규칙 정의 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/domain/ProjectSecurityAction.java` | 메서드별 액션 이름과 필요 권한 정의 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/domain/SecurityRequestContext.java` | 사용자 ID, 역할, 인증 상태를 담는 요청 컨텍스트 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/dto/ProjectOperationPayload.java` | 정상 응답과 보안 검증 이력을 함께 반환하는 DTO |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/dto/SecurityErrorResponse.java` | 실패 응답(상태 코드, 오류명, 메시지, 경로) DTO |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/exception/UnauthenticatedAccessException.java` | 인증 실패 시 발생하는 예외 |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/exception/UnauthorizedProjectAccessException.java` | 권한 부족 시 발생하는 예외 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/service/ProtectedProjectService.java` | 프록시와 실제 서비스가 공통으로 구현하는 인터페이스 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/service/ProjectSecurityVerifier.java` | 인증/권한 검사 로직을 담당하는 보안 검증기 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/service/ProtectedProjectServiceImpl.java` | 실제 비즈니스 작업을 수행하는 대상 서비스 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/service/SecurityVerificationProjectServiceProxy.java` | 대상 서비스 호출 전후에 보안 검증과 로그를 넣는 프록시 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/ProxySecurityControllerTest.java` | 정상/인증 실패/권한 실패 API 시나리오 검증 |

## 3. 구현 단계와 주요 코드 해설

1. `ProtectedProjectService` 인터페이스를 기준으로 보호 대상 메서드를 정의했습니다.
   - `viewProject()`는 일반 조회 작업이라 `USER` 이상이면 접근할 수 있게 잡았습니다.
   - `rotateSecrets()`는 민감한 운영 작업이라 `ADMIN` 권한을 요구하도록 분리했습니다.

2. `ProtectedProjectServiceImpl`에는 실제 비즈니스 처리만 넣었습니다.
   - 프로젝트 조회, 시크릿 재발급 같은 핵심 로직 결과만 반환합니다.
   - 인증, 권한, 감사 로그 같은 횡단 관심사는 직접 넣지 않았습니다.

3. `SecurityVerificationProjectServiceProxy`가 같은 인터페이스를 구현하면서 대상 서비스를 감쌌습니다.
   - 진입 로그 추가
   - 인증 검사
   - 권한 검사
   - 대상 서비스 호출
   - 사후 로그 기록
   위 순서를 한 메서드(`execute`)로 공통화해 두 메서드가 같은 검증 흐름을 사용하도록 만들었습니다.

4. `ProjectSecurityVerifier`에서 인증/권한 검사를 별도 클래스로 분리했습니다.
   - 인증 실패는 `UnauthenticatedAccessException`
   - 권한 부족은 `UnauthorizedProjectAccessException`
   예외 종류를 나눠 두면 HTTP 상태 코드를 401과 403으로 다르게 응답할 수 있습니다.

5. `@Primary`와 `@Qualifier`를 사용해 컨트롤러가 프록시를 주입받도록 구성했습니다.
   - 컨트롤러는 `ProtectedProjectService` 인터페이스만 의존합니다.
   - 실제 구현체 선택은 스프링 컨테이너가 담당합니다.
   - 이렇게 해야 컨트롤러는 프록시 존재를 몰라도 항상 보안 검증이 선행됩니다.

요청 흐름 요약:

1. `ProxySecurityController`가 요청 파라미터를 `SecurityRequestContext`로 묶습니다.
2. 컨트롤러는 `ProtectedProjectService`를 호출합니다.
3. 실제 주입된 빈은 `SecurityVerificationProjectServiceProxy`입니다.
4. 프록시는 `ProjectSecurityVerifier`로 인증/권한을 확인합니다.
5. 검증이 통과하면 `ProtectedProjectServiceImpl`을 호출합니다.
6. 응답 DTO에 보안 검증 이력을 붙여 반환합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `ProxySecurityController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/controller/ProxySecurityController.java`
- 역할: 프로젝트 조회/시크릿 재발급 API 진입점
- 상세 설명:
- 기본 경로: `/mission06/task01/proxy-security`
- 매핑 메서드:
  - `GET /projects/{projectId}` -> 조회
  - `POST /projects/{projectId}/rotate-secrets` -> 민감 작업
- 요청 파라미터 `userId`, `role`, `authenticated`를 받아 `SecurityRequestContext`로 변환한 뒤 서비스에 전달합니다.

<details>
<summary><code>ProxySecurityController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.AccessRole;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.ProjectOperationPayload;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service.ProtectedProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task01/proxy-security")
public class ProxySecurityController {

    private final ProtectedProjectService protectedProjectService;

    public ProxySecurityController(ProtectedProjectService protectedProjectService) {
        this.protectedProjectService = protectedProjectService;
    }

    @GetMapping("/projects/{projectId}")
    public ProjectOperationPayload viewProject(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "guest-user") String userId,
            @RequestParam(defaultValue = "USER") String role,
            @RequestParam(defaultValue = "true") boolean authenticated
    ) {
        return protectedProjectService.viewProject(projectId, createContext(userId, role, authenticated));
    }

    @PostMapping("/projects/{projectId}/rotate-secrets")
    public ProjectOperationPayload rotateSecrets(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "ops-admin") String userId,
            @RequestParam(defaultValue = "ADMIN") String role,
            @RequestParam(defaultValue = "true") boolean authenticated
    ) {
        return protectedProjectService.rotateSecrets(projectId, createContext(userId, role, authenticated));
    }

    private SecurityRequestContext createContext(String userId, String role, boolean authenticated) {
        return new SecurityRequestContext(userId, AccessRole.from(role), authenticated);
    }
}
```

</details>

### 4.2 `ProxySecurityExceptionHandler.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/controller/ProxySecurityExceptionHandler.java`
- 역할: 401/403/400 오류를 JSON 응답으로 변환
- 상세 설명:
- 컨트롤러에서 발생한 인증 실패, 권한 실패, 잘못된 입력을 각각 다른 상태 코드로 내려줍니다.
- 예외 메시지를 그대로 응답에 포함해 어떤 검증에서 막혔는지 바로 확인할 수 있습니다.
- 요청 URI를 함께 반환해 어떤 API에서 실패했는지도 추적할 수 있습니다.

<details>
<summary><code>ProxySecurityExceptionHandler.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.SecurityErrorResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception.UnauthenticatedAccessException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception.UnauthorizedProjectAccessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ProxySecurityController.class)
public class ProxySecurityExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthenticatedAccessException.class)
    public SecurityErrorResponse handleUnauthenticated(
            UnauthenticatedAccessException exception,
            HttpServletRequest request
    ) {
        return new SecurityErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHENTICATED",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UnauthorizedProjectAccessException.class)
    public SecurityErrorResponse handleUnauthorized(
            UnauthorizedProjectAccessException exception,
            HttpServletRequest request
    ) {
        return new SecurityErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public SecurityErrorResponse handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new SecurityErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
```

</details>

### 4.3 `AccessRole.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/domain/AccessRole.java`
- 역할: 역할 계층과 문자열 파싱 규칙 정의
- 상세 설명:
- `GUEST < USER < MANAGER < ADMIN` 순서로 권한 레벨을 숫자로 표현합니다.
- `hasAtLeast()`는 현재 역할이 요구 역할 이상인지 비교할 때 사용합니다.
- `from()`은 요청 파라미터 문자열을 enum으로 바꾸며, 허용되지 않은 값이면 바로 400 오류로 연결됩니다.

<details>
<summary><code>AccessRole.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain;

import java.util.Locale;

public enum AccessRole {

    GUEST(0),
    USER(1),
    MANAGER(2),
    ADMIN(3);

    private final int level;

    AccessRole(int level) {
        this.level = level;
    }

    public boolean hasAtLeast(AccessRole requiredRole) {
        return this.level >= requiredRole.level;
    }

    public static AccessRole from(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new IllegalArgumentException("role 파라미터는 비어 있을 수 없습니다.");
        }

        try {
            return AccessRole.valueOf(rawRole.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("지원하지 않는 role 입니다. 사용 가능 값: GUEST, USER, MANAGER, ADMIN");
        }
    }
}
```

</details>

### 4.4 `ProjectSecurityAction.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/domain/ProjectSecurityAction.java`
- 역할: 메서드별 액션 이름과 필요 권한 정의
- 상세 설명:
- 프록시가 어떤 메서드를 감싸는지, 그 메서드에 어떤 권한이 필요한지 한 곳에 모아 둔 정책 테이블입니다.
- `VIEW_PROJECT`와 `ROTATE_SECRETS`를 분리해 메서드별 보안 요구사항 차이를 코드로 드러냅니다.
- 응답 메시지와 검증 메시지에서도 같은 액션 라벨을 재사용합니다.

<details>
<summary><code>ProjectSecurityAction.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain;

public enum ProjectSecurityAction {

    VIEW_PROJECT("프로젝트 조회", AccessRole.USER),
    ROTATE_SECRETS("시크릿 재발급", AccessRole.ADMIN);

    private final String actionLabel;
    private final AccessRole requiredRole;

    ProjectSecurityAction(String actionLabel, AccessRole requiredRole) {
        this.actionLabel = actionLabel;
        this.requiredRole = requiredRole;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public AccessRole getRequiredRole() {
        return requiredRole;
    }
}
```

</details>

### 4.5 `SecurityRequestContext.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/domain/SecurityRequestContext.java`
- 역할: 사용자 ID, 역할, 인증 상태를 담는 요청 컨텍스트
- 상세 설명:
- 프록시와 검증기가 HTTP 레이어에 직접 의존하지 않도록 요청 정보를 별도 객체로 옮겼습니다.
- 사용자 식별자, 권한, 로그인 여부만 담고 있어 테스트에서도 쉽게 생성할 수 있습니다.
- 보안 검사에 필요한 최소 정보만 포함하므로 책임이 단순합니다.

<details>
<summary><code>SecurityRequestContext.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain;

public class SecurityRequestContext {

    private final String userId;
    private final AccessRole role;
    private final boolean authenticated;

    public SecurityRequestContext(String userId, AccessRole role, boolean authenticated) {
        this.userId = userId;
        this.role = role;
        this.authenticated = authenticated;
    }

    public String getUserId() {
        return userId;
    }

    public AccessRole getRole() {
        return role;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
```

</details>

### 4.6 `ProjectOperationPayload.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/dto/ProjectOperationPayload.java`
- 역할: 정상 응답과 보안 검증 이력을 함께 반환하는 DTO
- 상세 설명:
- 실제 서비스는 핵심 작업 결과만 담아 생성하고, 프록시는 `withSecurityMetadata()`로 보안 메타데이터를 붙입니다.
- `proxyApplied`, `requestedRole`, `requiredRole`, `securityChecks`를 같이 내려 주기 때문에 프록시가 어떤 일을 했는지 응답만 보고 확인할 수 있습니다.
- `List.copyOf()`로 보안 검증 이력을 불변 리스트처럼 다뤄 외부 수정 가능성을 줄였습니다.

<details>
<summary><code>ProjectOperationPayload.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto;

import java.util.List;

public class ProjectOperationPayload {

    private final String projectId;
    private final String action;
    private final String resultMessage;
    private final String requestedBy;
    private final String requestedRole;
    private final String requiredRole;
    private final boolean proxyApplied;
    private final List<String> securityChecks;

    public ProjectOperationPayload(String projectId, String action, String resultMessage) {
        this(projectId, action, resultMessage, null, null, null, false, List.of());
    }

    private ProjectOperationPayload(
            String projectId,
            String action,
            String resultMessage,
            String requestedBy,
            String requestedRole,
            String requiredRole,
            boolean proxyApplied,
            List<String> securityChecks
    ) {
        this.projectId = projectId;
        this.action = action;
        this.resultMessage = resultMessage;
        this.requestedBy = requestedBy;
        this.requestedRole = requestedRole;
        this.requiredRole = requiredRole;
        this.proxyApplied = proxyApplied;
        this.securityChecks = List.copyOf(securityChecks);
    }

    public ProjectOperationPayload withSecurityMetadata(
            String requestedBy,
            String requestedRole,
            String requiredRole,
            boolean proxyApplied,
            List<String> securityChecks
    ) {
        return new ProjectOperationPayload(
                projectId,
                action,
                resultMessage,
                requestedBy,
                requestedRole,
                requiredRole,
                proxyApplied,
                securityChecks
        );
    }

    public String getProjectId() {
        return projectId;
    }

    public String getAction() {
        return action;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public String getRequestedRole() {
        return requestedRole;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public boolean isProxyApplied() {
        return proxyApplied;
    }

    public List<String> getSecurityChecks() {
        return securityChecks;
    }
}
```

</details>

### 4.7 `SecurityErrorResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/dto/SecurityErrorResponse.java`
- 역할: 실패 응답(상태 코드, 오류명, 메시지, 경로) DTO
- 상세 설명:
- 인증 실패와 권한 실패를 구조화된 JSON으로 내려주기 위한 응답 전용 객체입니다.
- 어떤 요청이 왜 실패했는지 클라이언트가 바로 확인할 수 있게 최소 정보만 포함합니다.
- 컨트롤러 예외 처리기와 함께 사용됩니다.

<details>
<summary><code>SecurityErrorResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto;

public class SecurityErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public SecurityErrorResponse(int status, String error, String message, String path) {
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

### 4.8 `UnauthenticatedAccessException.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/exception/UnauthenticatedAccessException.java`
- 역할: 인증 실패 시 발생하는 예외
- 상세 설명:
- 로그인되지 않은 요청을 프록시 단계에서 중단시키기 위한 전용 예외입니다.
- 권한 부족 예외와 분리해 두면 401과 403을 구분해 반환할 수 있습니다.

<details>
<summary><code>UnauthenticatedAccessException.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception;

public class UnauthenticatedAccessException extends RuntimeException {

    public UnauthenticatedAccessException(String message) {
        super(message);
    }
}
```

</details>

### 4.9 `UnauthorizedProjectAccessException.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/exception/UnauthorizedProjectAccessException.java`
- 역할: 권한 부족 시 발생하는 예외
- 상세 설명:
- 인증은 되었지만 필요한 역할에 도달하지 못한 경우에 사용합니다.
- 예외 메시지에 요구 권한을 담아 어떤 정책에 걸렸는지 쉽게 알 수 있게 했습니다.

<details>
<summary><code>UnauthorizedProjectAccessException.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception;

public class UnauthorizedProjectAccessException extends RuntimeException {

    public UnauthorizedProjectAccessException(String message) {
        super(message);
    }
}
```

</details>

### 4.10 `ProtectedProjectService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/service/ProtectedProjectService.java`
- 역할: 프록시와 실제 서비스가 공통으로 구현하는 인터페이스
- 상세 설명:
- 프록시 패턴의 핵심인 "같은 인터페이스를 구현하는 대리 객체" 구조를 위한 기준점입니다.
- 컨트롤러는 구현체를 몰라도 이 인터페이스만 의존하면 됩니다.
- 조회 메서드와 민감 작업 메서드를 함께 정의해 메서드별 보안 정책 비교가 가능하게 했습니다.

<details>
<summary><code>ProtectedProjectService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.ProjectOperationPayload;

public interface ProtectedProjectService {

    ProjectOperationPayload viewProject(String projectId, SecurityRequestContext context);

    ProjectOperationPayload rotateSecrets(String projectId, SecurityRequestContext context);
}
```

</details>

### 4.11 `ProjectSecurityVerifier.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/service/ProjectSecurityVerifier.java`
- 역할: 인증/권한 검사 로직을 담당하는 보안 검증기
- 상세 설명:
- 핵심 공개 메서드: `verifyAuthentication()`, `verifyAuthorization()`
- 인증 여부와 역할 비교를 분리해 프록시가 너무 많은 책임을 지지 않도록 했습니다.
- 보안 검증 과정을 `securityChecks` 리스트에 남겨 응답에서 실제 수행 순서를 확인할 수 있습니다.

<details>
<summary><code>ProjectSecurityVerifier.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.ProjectSecurityAction;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception.UnauthenticatedAccessException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.exception.UnauthorizedProjectAccessException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProjectSecurityVerifier {

    public void verifyAuthentication(
            SecurityRequestContext context,
            ProjectSecurityAction action,
            List<String> securityChecks
    ) {
        securityChecks.add("인증 검사 시작: 사용자=" + context.getUserId());

        if (!context.isAuthenticated()) {
            securityChecks.add("인증 실패: 로그인되지 않은 요청");
            throw new UnauthenticatedAccessException(action.getActionLabel() + " 작업은 로그인 후에만 실행할 수 있습니다.");
        }

        securityChecks.add("인증 성공");
    }

    public void verifyAuthorization(
            SecurityRequestContext context,
            ProjectSecurityAction action,
            List<String> securityChecks
    ) {
        securityChecks.add(
                "권한 검사 시작: 요청 역할=" + context.getRole().name()
                        + ", 필요 역할=" + action.getRequiredRole().name()
        );

        if (!context.getRole().hasAtLeast(action.getRequiredRole())) {
            securityChecks.add("권한 거부");
            throw new UnauthorizedProjectAccessException(
                    action.getActionLabel() + " 작업에는 " + action.getRequiredRole().name() + " 권한이 필요합니다."
            );
        }

        securityChecks.add("권한 승인");
    }
}
```

</details>

### 4.12 `ProtectedProjectServiceImpl.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/service/ProtectedProjectServiceImpl.java`
- 역할: 실제 비즈니스 작업을 수행하는 대상 서비스
- 상세 설명:
- 핵심 공개 메서드: `viewProject()`, `rotateSecrets()`
- 인증과 권한 로직 없이 실제 작업 결과만 만들어 반환합니다.
- 프록시가 감싸기 전에는 일반 서비스처럼 동작하는 대상 객체(Real Subject) 역할을 합니다.

<details>
<summary><code>ProtectedProjectServiceImpl.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.ProjectSecurityAction;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.ProjectOperationPayload;
import org.springframework.stereotype.Service;

@Service("protectedProjectServiceTarget")
public class ProtectedProjectServiceImpl implements ProtectedProjectService {

    @Override
    public ProjectOperationPayload viewProject(String projectId, SecurityRequestContext context) {
        return new ProjectOperationPayload(
                projectId,
                ProjectSecurityAction.VIEW_PROJECT.getActionLabel(),
                "프로젝트 " + projectId + "의 운영 상태와 배포 이력을 조회했습니다."
        );
    }

    @Override
    public ProjectOperationPayload rotateSecrets(String projectId, SecurityRequestContext context) {
        return new ProjectOperationPayload(
                projectId,
                ProjectSecurityAction.ROTATE_SECRETS.getActionLabel(),
                "프로젝트 " + projectId + "의 배포 시크릿을 재발급하고 감사 로그를 남겼습니다."
        );
    }
}
```

</details>

### 4.13 `SecurityVerificationProjectServiceProxy.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/service/SecurityVerificationProjectServiceProxy.java`
- 역할: 대상 서비스 호출 전후에 보안 검증과 로그를 넣는 프록시
- 상세 설명:
- 핵심 공개 메서드: `viewProject()`, `rotateSecrets()`
- 내부 `execute()` 메서드에서 인증/권한 검사, 대상 호출, 사후 로그 기록을 같은 순서로 처리합니다.
- `@Primary`로 등록되어 컨트롤러는 이 프록시를 우선 주입받고, `@Qualifier("protectedProjectServiceTarget")`로 실제 대상 서비스를 분리 주입합니다.

<details>
<summary><code>SecurityVerificationProjectServiceProxy.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.ProjectSecurityAction;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.domain.SecurityRequestContext;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto.ProjectOperationPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class SecurityVerificationProjectServiceProxy implements ProtectedProjectService {

    private final ProtectedProjectService target;
    private final ProjectSecurityVerifier projectSecurityVerifier;

    public SecurityVerificationProjectServiceProxy(
            @Qualifier("protectedProjectServiceTarget") ProtectedProjectService target,
            ProjectSecurityVerifier projectSecurityVerifier
    ) {
        this.target = target;
        this.projectSecurityVerifier = projectSecurityVerifier;
    }

    @Override
    public ProjectOperationPayload viewProject(String projectId, SecurityRequestContext context) {
        return execute(ProjectSecurityAction.VIEW_PROJECT, projectId, context, target::viewProject);
    }

    @Override
    public ProjectOperationPayload rotateSecrets(String projectId, SecurityRequestContext context) {
        return execute(ProjectSecurityAction.ROTATE_SECRETS, projectId, context, target::rotateSecrets);
    }

    private ProjectOperationPayload execute(
            ProjectSecurityAction action,
            String projectId,
            SecurityRequestContext context,
            BiFunction<String, SecurityRequestContext, ProjectOperationPayload> invocation
    ) {
        List<String> securityChecks = new ArrayList<>();
        securityChecks.add("프록시 진입: " + action.getActionLabel());

        projectSecurityVerifier.verifyAuthentication(context, action, securityChecks);
        projectSecurityVerifier.verifyAuthorization(context, action, securityChecks);

        securityChecks.add("대상 서비스 호출 전");
        ProjectOperationPayload payload = invocation.apply(projectId, context);
        securityChecks.add("대상 서비스 호출 후");
        securityChecks.add("사후 보안 로그 기록 완료");

        return payload.withSecurityMetadata(
                context.getUserId(),
                context.getRole().name(),
                action.getRequiredRole().name(),
                true,
                securityChecks
        );
    }
}
```

</details>

### 4.14 `ProxySecurityControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission06_spring_core_advanced/task01_proxy_security/ProxySecurityControllerTest.java`
- 역할: 정상/인증 실패/권한 실패 API 시나리오 검증
- 상세 설명:
- 검증 시나리오:
  - `viewProject_whenAuthenticatedUser_thenReturnsSecurityTrace`
  - `rotateSecrets_whenAdmin_thenReturnsProtectedResult`
  - `rotateSecrets_whenUnauthenticated_thenReturns401`
  - `rotateSecrets_whenRoleIsUser_thenReturns403`
- 정상 흐름에서 프록시가 보안 이력을 응답에 남기는지 확인하고, 예외 흐름에서는 401/403 분리가 유지되는지 보장합니다.

<details>
<summary><code>ProxySecurityControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ProxySecurityControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void viewProject_whenAuthenticatedUser_thenReturnsSecurityTrace() throws Exception {
        mockMvc.perform(get("/mission06/task01/proxy-security/projects/project-alpha")
                        .param("userId", "analyst-kim")
                        .param("role", "USER")
                        .param("authenticated", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value("project-alpha"))
                .andExpect(jsonPath("$.action").value("프로젝트 조회"))
                .andExpect(jsonPath("$.requestedBy").value("analyst-kim"))
                .andExpect(jsonPath("$.requestedRole").value("USER"))
                .andExpect(jsonPath("$.requiredRole").value("USER"))
                .andExpect(jsonPath("$.proxyApplied").value(true))
                .andExpect(jsonPath("$.securityChecks", hasSize(8)))
                .andExpect(jsonPath("$.securityChecks[0]").value("프록시 진입: 프로젝트 조회"))
                .andExpect(jsonPath("$.securityChecks[7]").value("사후 보안 로그 기록 완료"));
    }

    @Test
    void rotateSecrets_whenAdmin_thenReturnsProtectedResult() throws Exception {
        mockMvc.perform(post("/mission06/task01/proxy-security/projects/project-alpha/rotate-secrets")
                        .param("userId", "ops-admin")
                        .param("role", "ADMIN")
                        .param("authenticated", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("시크릿 재발급"))
                .andExpect(jsonPath("$.requestedRole").value("ADMIN"))
                .andExpect(jsonPath("$.requiredRole").value("ADMIN"))
                .andExpect(jsonPath("$.resultMessage").value("프로젝트 project-alpha의 배포 시크릿을 재발급하고 감사 로그를 남겼습니다."));
    }

    @Test
    void rotateSecrets_whenUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(post("/mission06/task01/proxy-security/projects/project-alpha/rotate-secrets")
                        .param("userId", "anonymous")
                        .param("role", "ADMIN")
                        .param("authenticated", "false"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("시크릿 재발급 작업은 로그인 후에만 실행할 수 있습니다."));
    }

    @Test
    void rotateSecrets_whenRoleIsUser_thenReturns403() throws Exception {
        mockMvc.perform(post("/mission06/task01/proxy-security/projects/project-alpha/rotate-secrets")
                        .param("userId", "team-user")
                        .param("role", "USER")
                        .param("authenticated", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("시크릿 재발급 작업에는 ADMIN 권한이 필요합니다."));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **프록시 패턴**
  - 핵심: 실제 객체 대신 같은 인터페이스를 구현한 대리 객체가 먼저 호출을 받아 공통 작업을 수행한 뒤 대상 객체에 위임하는 구조입니다.
  - 왜 쓰는가: 인증, 권한, 로깅처럼 여러 메서드에 반복되는 관심사를 실제 비즈니스 로직과 분리할 수 있습니다.
  - 참고 링크:
    - Spring Framework Proxying Mechanisms: https://docs.spring.io/spring-framework/reference/core/aop/proxying.html

- **인증(Authentication)과 인가(Authorization) 분리**
  - 핵심: 인증은 "누구인지 확인", 인가는 "이 작업을 해도 되는지 확인"입니다.
  - 왜 쓰는가: 실패 이유를 정확히 나누면 401과 403을 올바르게 구분할 수 있고, 보안 정책도 더 명확해집니다.
  - 참고 링크:
    - Spring Security Authentication Architecture: https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html
    - Spring Security Authorization Architecture: https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html

- **`@Primary`와 `@Qualifier`**
  - 핵심: 같은 타입의 빈이 여러 개일 때 어떤 빈을 기본 주입 대상으로 쓸지, 또는 특정 빈을 명시적으로 선택할지 정하는 방식입니다.
  - 왜 쓰는가: 컨트롤러는 프록시를 기본으로 받되, 프록시 내부에서는 실제 대상 서비스만 정확히 주입받아야 하기 때문입니다.
  - 참고 링크:
    - Spring Framework Fine-tuning Annotation-based Autowiring with `@Primary`: https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired-primary.html
    - Spring Framework Fine-tuning Annotation-based Autowiring with Qualifiers: https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired-qualifiers.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 프로젝트 조회 호출

```bash
curl "http://localhost:8080/mission06/task01/proxy-security/projects/project-alpha?userId=analyst-kim&role=USER&authenticated=true"
```

예상 결과:

- HTTP 200
- `proxyApplied=true`
- `requiredRole=USER`
- `securityChecks`에 인증/권한/사후 로그가 순서대로 포함됨

### 6.3 시크릿 재발급 성공 호출

```bash
curl -X POST "http://localhost:8080/mission06/task01/proxy-security/projects/project-alpha/rotate-secrets?userId=ops-admin&role=ADMIN&authenticated=true"
```

예상 결과:

- HTTP 200
- `requiredRole=ADMIN`
- `resultMessage`에 시크릿 재발급 완료 메시지가 포함됨

### 6.4 시크릿 재발급 권한 실패 호출

```bash
curl -X POST "http://localhost:8080/mission06/task01/proxy-security/projects/project-alpha/rotate-secrets?userId=team-user&role=USER&authenticated=true"
```

예상 결과:

- HTTP 403
- `error=FORBIDDEN`
- `message=시크릿 재발급 작업에는 ADMIN 권한이 필요합니다.`

### 6.5 시크릿 재발급 인증 실패 호출

```bash
curl -X POST "http://localhost:8080/mission06/task01/proxy-security/projects/project-alpha/rotate-secrets?userId=anonymous&role=ADMIN&authenticated=false"
```

예상 결과:

- HTTP 401
- `error=UNAUTHENTICATED`
- `message=시크릿 재발급 작업은 로그인 후에만 실행할 수 있습니다.`

### 6.6 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.ProxySecurityControllerTest
```

예상 결과:

- `BUILD SUCCESSFUL`
- 4개 테스트 시나리오 통과

## 7. 결과 확인 방법

- 성공 기준:
  - 조회 API는 `USER` 권한으로 200 응답을 반환해야 합니다.
  - 시크릿 재발급은 `ADMIN`일 때만 200, `USER`일 때는 403, 인증되지 않으면 401이어야 합니다.
  - 성공 응답에는 `securityChecks` 배열이 포함되어 프록시가 전후 검증을 수행했음을 보여줘야 합니다.

- 응답 스냅샷 파일:
  - `docs/mission-06-spring-core-advanced/task-01-proxy-security/responses/view-project-success.txt`
  - `docs/mission-06-spring-core-advanced/task-01-proxy-security/responses/rotate-secrets-success.txt`
  - `docs/mission-06-spring-core-advanced/task-01-proxy-security/responses/rotate-secrets-forbidden.txt`
  - `docs/mission-06-spring-core-advanced/task-01-proxy-security/responses/rotate-secrets-unauthenticated.txt`

- 테스트 로그 파일:
  - `docs/mission-06-spring-core-advanced/task-01-proxy-security/task01-gradle-test-output.txt`

## 8. 학습 내용

- 프록시 패턴은 "대상 객체를 바꾸지 않고 앞단에 공통 로직을 끼워 넣는 방법"이라는 점이 핵심입니다. 실제 서비스는 본래 책임만 유지하고, 인증/권한/로그는 프록시가 맡으니 구조가 더 선명해집니다.
- 인증과 인가를 분리하면 실패 이유가 분명해집니다. 로그인 자체가 안 된 경우와, 로그인은 했지만 권한이 부족한 경우를 같은 오류로 처리하지 않게 됩니다.
- 컨트롤러가 인터페이스에만 의존하고 실제 주입 대상은 스프링 컨테이너가 고르게 하면, 나중에 동적 프록시나 AOP로 바꿔도 호출부 코드를 거의 건드리지 않아도 됩니다.
- 이번 구조는 스프링 AOP를 배우기 전 단계로도 의미가 있습니다. AOP가 내부적으로 어떤 문제를 해결하는지, 왜 프록시 기반으로 동작하는지 수동 구현을 통해 먼저 체감할 수 있습니다.
