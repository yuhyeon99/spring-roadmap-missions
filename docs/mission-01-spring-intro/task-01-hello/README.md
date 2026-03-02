# Spring Boot "Hello, World!" API 설정

이 문서는 `mission-01-spring-intro`의 `task-01-hello`를 수작업 기준으로 다시 정리한 보고서입니다.
태스크별 의도와 코드 흐름을 중심으로 설명하고, 모든 관련 파일은 토글 코드 블록으로 확인할 수 있습니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-01-hello`
- 목표:
  - 스프링 부트 애플리케이션에서 가장 단순한 REST 엔드포인트를 노출한다.
  - 컨트롤러 매핑과 문자열 응답 직렬화가 어떻게 동작하는지 확인한다.
- 엔드포인트: `GET /hello`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Etc | `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task01_hello/HelloWorldController.java` | 요청 진입점(HTTP 매핑/응답 구성) |

## 3. 구현 단계와 주요 코드 해설

1. `HelloWorldController`를 `@RestController`로 선언해 JSON/문자열 응답 가능한 컨트롤러 빈으로 등록합니다.
2. `@GetMapping("/hello")`로 URI를 매핑해 DispatcherServlet → HandlerMapping → Controller 흐름을 확인합니다.
3. 메서드 반환값("Hello, World!")이 HTTP 응답 본문으로 직렬화되는 최소 경로를 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `HelloWorldController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task01_hello/HelloWorldController.java`
- 역할: 요청 진입점(HTTP 매핑/응답 구성)
- 상세 설명:
- 기본 경로: `/hello`
- 매핑 메서드: Get
- 컨트롤러는 입력을 바인딩하고 서비스 결과를 HTTP 응답 규약에 맞춰 반환합니다.

<details>
<summary><code>HelloWorldController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission01_spring_intro.task01_hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello, World!";
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **`@RestController`**
  - 핵심: 컨트롤러 반환값을 HTTP 응답 본문으로 바로 직렬화합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html
- **`@GetMapping`**
  - 핵심: 특정 URI를 GET 메서드와 매핑합니다.
  - 참고: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html

## 6. 실행·검증 방법

### 6.1 실행

```bash
./gradlew bootRun
```

### 6.2 호출 예시

```bash
curl http://localhost:8080/hello
```

예상 결과: `Hello, World!`

## 7. 결과 확인 방법

- 브라우저 또는 curl로 `/hello`를 호출해 문자열 응답을 확인합니다.
- 초기 실습 캡처가 필요하면 `docs/mission-01-spring-intro/task-01-hello/`에 PNG를 저장합니다.

## 8. 학습 내용

- 가장 작은 엔드포인트를 직접 구성하면 스프링 웹 요청 처리 파이프라인을 빠르게 이해할 수 있습니다.
- 이후 태스크에서 계층이 늘어나더라도 컨트롤러 매핑과 응답 반환 원리는 동일하게 유지됩니다.
