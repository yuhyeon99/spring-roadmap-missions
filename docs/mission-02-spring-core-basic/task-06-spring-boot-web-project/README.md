# 스프링 핵심 원리 - 기본: 스프링 부트를 사용하여 웹 애플리케이션 프로젝트 생성하기

이 문서는 `mission-02-spring-core-basic`의 `task-06-spring-boot-web-project` 작업 내용을 정리한 보고서입니다.  
Spring Boot 기반 웹 애플리케이션 구성을 확인하고, 프로젝트 생성 시 필요한 의존성을 추가한 뒤 API로 동작을 검증했습니다.

## 1. 작업 개요

- 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project`
- 목표:
  - Spring Boot 웹 프로젝트에 필요한 의존성을 명시적으로 추가한다.
  - 프로젝트 구성 정보(기본 패키지/의존성)를 API로 확인한다.
  - `spring-boot-starter-validation` 의존성 추가 효과를 `@Valid` 검증으로 확인한다.
- 시나리오:
  - `GET /mission02/task06/project-bootstrap`으로 구성 요약 조회
  - `POST /mission02/task06/project-bootstrap`으로 입력 검증 포함 생성 요청 처리

## 2. 구현 단계와 주요 코드

### 2.1 프로젝트 의존성 추가

- `build.gradle`

추가한 의존성:
- `spring-boot-starter-web`: 웹 요청/응답 처리
- `spring-boot-starter-thymeleaf`: 서버 템플릿 렌더링 지원
- `spring-boot-starter-validation`: 요청 DTO 검증 지원
- `spring-boot-starter-test`: 테스트 실행 환경

특히 이번 테스크에서는 `spring-boot-starter-validation`을 새로 추가하고 `@Valid` 검증 흐름을 구현했습니다.

### 2.2 프로젝트 부트스트랩 API 구성

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/controller/ProjectBootstrapController.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/service/ProjectBootstrapService.java`

`ProjectBootstrapController`는 다음 역할을 수행합니다.
- `GET`: 프로젝트 기본 정보와 의존성 목록 반환
- `POST`: 생성 요청 데이터를 검증한 뒤 생성 완료 응답 반환

### 2.3 검증용 요청/응답 DTO 작성

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/ProjectCreateRequest.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/ProjectCreateResponse.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/ProjectBootstrapResponse.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/dto/DependencyItem.java`

`ProjectCreateRequest`에는 `@NotBlank`, `@Size`를 적용해 필수값/길이 제한을 검증합니다.

### 2.4 테스트로 의존성/검증 동작 확인

- `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task06_spring_boot_web_project/ProjectBootstrapServiceTest.java`

검증 항목:
- 프로젝트 구성 요약에 필수 의존성(`web`, `validation`)이 포함되는지 확인
- 생성 요청 데이터의 공백 제거(trim) 처리 확인
- `jakarta.validation`으로 공백 `projectName`이 검증 실패하는지 확인

## 3. 실행·빌드·테스트 방법과 예상 결과

### 3.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3.2 API 호출 예시

구성 요약 조회:

```bash
curl http://localhost:8080/mission02/task06/project-bootstrap
```

예상 응답(요약):

```json
{
  "task": "mission02 task06 스프링 부트 웹 프로젝트 생성",
  "basePackage": "com.goorm.springmissionsplayground",
  "dependencies": [
    {"dependency":"spring-boot-starter-web","reason":"REST API 및 웹 요청 처리"},
    {"dependency":"spring-boot-starter-thymeleaf","reason":"서버 사이드 HTML 템플릿 렌더링"},
    {"dependency":"spring-boot-starter-validation","reason":"요청 데이터 검증(@Valid)"},
    {"dependency":"spring-boot-starter-test","reason":"테스트 코드 실행 환경"}
  ]
}
```

생성 요청:

```bash
curl -i -X POST http://localhost:8080/mission02/task06/project-bootstrap \
  -H "Content-Type: application/json" \
  -d '{"projectName":"mission02-task06-web","owner":"kim","description":"스프링 부트 웹 프로젝트 생성 실습"}'
```

예상 결과:
- HTTP `201 Created`
- 응답 본문에 `입력값 검증 완료` 포함

### 3.3 테스트 실행

```bash
./gradlew test --tests "*ProjectBootstrapServiceTest"
```

예상 결과:
- 테스트 3건 성공
- 의존성 구성/입력값 검증 동작 확인 완료

## 4. 결과 확인 방법

- `GET /mission02/task06/project-bootstrap` 응답의 `dependencies` 배열 확인
- `POST /mission02/task06/project-bootstrap` 요청 시 정상/비정상 입력을 각각 호출해 상태 코드 비교
- 테스트 리포트에서 `ProjectBootstrapServiceTest` 성공 여부 확인

## 학습 내용

- Spring Boot 프로젝트 생성 시 스타터 의존성을 목적에 맞게 조합하면 초기 설정을 크게 줄일 수 있습니다.
- `spring-boot-starter-web`은 내장 톰캣, MVC 설정, JSON 변환기를 포함해 웹 API 시작점을 제공합니다.
- `spring-boot-starter-validation`을 추가하면 `@Valid`와 Bean Validation 애너테이션으로 요청 검증을 표준 방식으로 처리할 수 있습니다.
- 의존성 선택 기준과 용도를 코드/응답으로 남겨두면, 프로젝트 초기 구성 의도를 팀 내에서 공유하기 쉽습니다.
