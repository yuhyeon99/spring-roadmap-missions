# Spring Boot "Hello, World!" API 설정

이 문서는 `mission-01-spring-intro`의 `task-01-hello` 구현을 동일 포맷으로 정리한 보고서입니다.
파일 경로 인덱스, 파일별 상세 설명, 핵심 개념 링크, 전체 코드 토글을 함께 제공합니다.

## 1. 작업 개요

- 미션/태스크: `mission-01-spring-intro` / `task-01-hello`
- 소스 패키지: `com.goorm.springmissionsplayground.mission01_spring_intro.task01_hello`
- 코드 파일 수(테스트 포함): **1개**

## 2. 코드 파일 경로 인덱스

| 파일 경로 | 역할 |
|---|---|
| `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task01_hello/HelloWorldController.java` | HTTP 요청을 받아 문자열 응답을 반환하는 REST 컨트롤러 |

## 3. 구현 흐름 요약

1. 컨트롤러(있다면)에서 요청을 수신하고 입력을 DTO/파라미터로 변환합니다.
2. 서비스 계층에서 핵심 규칙(검증, 계산, 트랜잭션, 정책 선택)을 수행합니다.
3. 저장소/도메인 계층과 협력해 상태를 조회·변경하고 결과를 응답으로 반환합니다.
4. 테스트 코드에서 정상/예외 흐름을 검증해 동작을 고정합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `HelloWorldController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task01_hello/HelloWorldController.java`
- 역할: HTTP 요청을 받아 문자열 응답을 반환하는 REST 컨트롤러
- 상세 설명:
- `GET /hello` 요청을 `helloWorld()` 메서드에 매핑해 기본 엔드포인트를 노출합니다.
- 스프링 MVC의 요청 처리 흐름(컨트롤러 매핑, 응답 직렬화) 학습용 최소 예제를 제공합니다.

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

- **`@RestController`와 요청 매핑**: 컨트롤러 메서드를 HTTP 엔드포인트로 노출합니다.  
  공식 문서: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- **Spring Boot 자동 구성(Auto Configuration)**: 웹 서버/DispatcherServlet을 기본 설정으로 자동 구성합니다.  
  공식 문서: https://docs.spring.io/spring-boot/reference/using/auto-configuration.html

## 6. 실행·빌드·테스트 방법

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행(참고):
- 현재 태스크 전용 테스트 파일은 없습니다. 필요하면 추후 테스트를 추가합니다.

## 7. 결과 확인 방법

- 컨트롤러가 있는 태스크는 API 호출(curl/브라우저)로 응답 구조와 상태 코드를 확인합니다.
- SQL 로그/애스펙트 로그/콘솔 출력이 필요한 태스크는 실행 로그를 함께 확인합니다.
- 필요 시 실행 결과를 캡처해 태스크 문서 디렉토리에 PNG로 저장합니다.

## 8. 학습 내용

- 파일 경로 인덱스를 먼저 확인하면 전체 구조를 빠르게 파악할 수 있습니다.
- 컨트롤러-서비스-저장소(또는 정책/도메인) 흐름을 분리하면 변경 지점을 명확히 관리할 수 있습니다.
- 공식 문서를 기준으로 개념을 확인하면서 코드와 연결하면 실습 재현성이 높아집니다.
