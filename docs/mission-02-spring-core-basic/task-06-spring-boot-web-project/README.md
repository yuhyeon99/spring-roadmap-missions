# 스프링 핵심 원리 - 기본: 스프링 부트를 사용하여 웹 애플리케이션 프로젝트 생성하기

이 문서는 `mission-02-spring-core-basic`의 `task-06-spring-boot-web-project` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-02-spring-core-basic` / `task-06-spring-boot-web-project`
- 소스 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project`
- 코드 파일 수(테스트 포함): **7개**
- 주요 API 베이스 경로:
  - `/mission02/task06/project-bootstrap` (ProjectBootstrapController.java)

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/controller/ProjectBootstrapController.java` | HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환 |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/DependencyItem.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/ProjectBootstrapResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/ProjectCreateRequest.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/ProjectCreateResponse.java` | 계층 간 데이터 전달 형식(요청/응답) |
| `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/service/ProjectBootstrapService.java` | 핵심 비즈니스 로직과 흐름 제어를 담당 |
| `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/ProjectBootstrapServiceTest.java` | 핵심 동작을 자동 검증하는 테스트 코드 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `ProjectBootstrapController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/controller/ProjectBootstrapController.java`
- 역할: HTTP 요청을 받아 입력을 바인딩하고 서비스 결과를 응답으로 반환
- 상세 설명:
- 요청 URI와 HTTP 메서드를 메서드에 매핑해 외부 진입점을 구성합니다.
- 요청 DTO/파라미터를 검증 가능한 형태로 서비스 계층에 전달합니다.
- 응답 상태 코드와 응답 DTO를 통해 API 계약을 고정합니다.

<details>
<summary><code>ProjectBootstrapController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectBootstrapResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.service.ProjectBootstrapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task06/project-bootstrap")
public class ProjectBootstrapController {

    private final ProjectBootstrapService projectBootstrapService;

    public ProjectBootstrapController(ProjectBootstrapService projectBootstrapService) {
        this.projectBootstrapService = projectBootstrapService;
    }

    @GetMapping
    public ProjectBootstrapResponse summary() {
        return projectBootstrapService.projectSummary();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectCreateResponse create(@Valid @RequestBody ProjectCreateRequest request) {
        return projectBootstrapService.create(request);
    }
}
```

</details>

### 4.2 `DependencyItem.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/DependencyItem.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>DependencyItem.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto;

public class DependencyItem {

    private final String dependency;
    private final String reason;

    public DependencyItem(String dependency, String reason) {
        this.dependency = dependency;
        this.reason = reason;
    }

    public String getDependency() {
        return dependency;
    }

    public String getReason() {
        return reason;
    }
}
```

</details>

### 4.3 `ProjectBootstrapResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/ProjectBootstrapResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>ProjectBootstrapResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto;

import java.util.List;

public class ProjectBootstrapResponse {

    private final String task;
    private final String basePackage;
    private final List<DependencyItem> dependencies;

    public ProjectBootstrapResponse(
            String task,
            String basePackage,
            List<DependencyItem> dependencies
    ) {
        this.task = task;
        this.basePackage = basePackage;
        this.dependencies = dependencies;
    }

    public String getTask() {
        return task;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public List<DependencyItem> getDependencies() {
        return dependencies;
    }
}
```

</details>

### 4.4 `ProjectCreateRequest.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/ProjectCreateRequest.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>ProjectCreateRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectCreateRequest {

    @NotBlank(message = "projectName은 필수입니다.")
    @Size(max = 40, message = "projectName은 40자 이하여야 합니다.")
    private String projectName;

    @NotBlank(message = "owner는 필수입니다.")
    @Size(max = 30, message = "owner는 30자 이하여야 합니다.")
    private String owner;

    @NotBlank(message = "description은 필수입니다.")
    @Size(max = 200, message = "description은 200자 이하여야 합니다.")
    private String description;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
```

</details>

### 4.5 `ProjectCreateResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/ProjectCreateResponse.java`
- 역할: 계층 간 데이터 전달 형식(요청/응답)
- 상세 설명:
- 요청/응답 전용 구조를 분리해 도메인 모델의 직접 노출을 방지합니다.
- API 스펙 변경이 도메인 내부 구조에 전파되지 않도록 완충 계층 역할을 합니다.

<details>
<summary><code>ProjectCreateResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto;

public class ProjectCreateResponse {

    private final String projectName;
    private final String owner;
    private final String message;
    private final String validation;

    public ProjectCreateResponse(String projectName, String owner, String message, String validation) {
        this.projectName = projectName;
        this.owner = owner;
        this.message = message;
        this.validation = validation;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getOwner() {
        return owner;
    }

    public String getMessage() {
        return message;
    }

    public String getValidation() {
        return validation;
    }
}
```

</details>

### 4.6 `ProjectBootstrapService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/service/ProjectBootstrapService.java`
- 역할: 핵심 비즈니스 로직과 흐름 제어를 담당
- 상세 설명:
- 비즈니스 규칙을 한 곳에 모아 컨트롤러와 저장소 책임을 분리합니다.
- 트랜잭션 경계, 예외 처리, 정책 선택 같은 핵심 흐름을 제어합니다.
- 테스트 시 서비스 단위로 핵심 동작을 검증하기 쉬운 구조를 제공합니다.

<details>
<summary><code>ProjectBootstrapService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.DependencyItem;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectBootstrapResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectBootstrapService {

    public ProjectBootstrapResponse projectSummary() {
        return new ProjectBootstrapResponse(
                "mission02 task06 스프링 부트 웹 프로젝트 생성",
                "com.goorm.springmissionsplayground",
                List.of(
                        new DependencyItem("spring-boot-starter-web", "REST API 및 웹 요청 처리"),
                        new DependencyItem("spring-boot-starter-thymeleaf", "서버 사이드 HTML 템플릿 렌더링"),
                        new DependencyItem("spring-boot-starter-validation", "요청 데이터 검증(@Valid)"),
                        new DependencyItem("spring-boot-starter-test", "테스트 코드 실행 환경")
                )
        );
    }

    public ProjectCreateResponse create(ProjectCreateRequest request) {
        return new ProjectCreateResponse(
                request.getProjectName().trim(),
                request.getOwner().trim(),
                "Spring Boot 웹 애플리케이션 초기 구성이 완료되었습니다.",
                "입력값 검증 완료"
        );
    }
}
```

</details>

### 4.7 `ProjectBootstrapServiceTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/ProjectBootstrapServiceTest.java`
- 역할: 핵심 동작을 자동 검증하는 테스트 코드
- 상세 설명:
- 요구사항을 테스트 시나리오로 고정해 회귀를 빠르게 감지합니다.
- 핵심 분기(정상/예외)를 검증해 구현 의도를 보장합니다.

<details>
<summary><code>ProjectBootstrapServiceTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectBootstrapResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.service.ProjectBootstrapService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectBootstrapServiceTest {

    private final ProjectBootstrapService projectBootstrapService = new ProjectBootstrapService();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void projectSummary_containsRequiredDependencies() {
        ProjectBootstrapResponse response = projectBootstrapService.projectSummary();

        assertThat(response.getTask()).isEqualTo("mission02 task06 스프링 부트 웹 프로젝트 생성");
        assertThat(response.getBasePackage()).isEqualTo("com.goorm.springmissionsplayground");
        assertThat(response.getDependencies()).hasSize(4);
        assertThat(response.getDependencies())
                .extracting("dependency")
                .contains("spring-boot-starter-web", "spring-boot-starter-validation");
    }

    @Test
    void create_trimsInputValues() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setProjectName("  mission02-task06-web  ");
        request.setOwner("  kim  ");
        request.setDescription("스프링 부트 웹 프로젝트 생성 실습");

        ProjectCreateResponse response = projectBootstrapService.create(request);

        assertThat(response.getProjectName()).isEqualTo("mission02-task06-web");
        assertThat(response.getOwner()).isEqualTo("kim");
        assertThat(response.getValidation()).isEqualTo("입력값 검증 완료");
    }

    @Test
    void createRequestValidation_rejectsBlankProjectName() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setProjectName("   ");
        request.setOwner("kim");
        request.setDescription("desc");

        assertThat(validator.validate(request))
                .extracting("message")
                .contains("projectName은 필수입니다.");
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **Spring Boot 프로젝트 구조**: 스타터 의존성으로 웹 애플리케이션 기본 구성을 빠르게 시작합니다.  
  공식 문서: https://docs.spring.io/spring-boot/reference/using/index.html
- **입력 검증(Bean Validation)**: DTO 제약 조건으로 잘못된 요청을 조기에 차단합니다.  
  공식 문서: https://jakarta.ee/specifications/bean-validation/

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(태스크 범위):

```bash
./gradlew test --tests "*task06_spring_boot_web_project*"
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
