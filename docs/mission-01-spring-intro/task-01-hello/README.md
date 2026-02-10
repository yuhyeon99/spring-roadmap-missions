# Spring Boot "Hello, World!" API 설정

이 문서는 Spring 입문 미션(`mission-01-spring-intro`)의 첫 번째 테스크(`task-01-hello`)에서 기본적인 "Hello, World!" REST API 엔드포인트를 설정한 과정을 정리합니다.

## 1. 컨트롤러 구현

`src/main/java/com/goorm/springbootintroproject/mission01_spring_intro/task01_hello/` 디렉토리에 `HelloWorldController.java` 파일이 생성되었습니다. 이 컨트롤러는 Spring의 `@RestController` 및 `@GetMapping` 어노테이션을 사용하여 간단한 엔드포인트를 노출합니다.

**파일: `src/main/java/com/goorm/springbootintroproject/mission01_spring_intro/task01_hello/HelloWorldController.java`**

```java
package com.goorm.springbootintroproject.mission01_spring_intro.task01_hello;

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

## 2. 빌드 구성 업데이트

`build.gradle` 파일은 Spring Boot로 웹 애플리케이션을 개발하는 데 필수적인 `spring-boot-starter-web` 의존성을 포함하도록 업데이트되었습니다.

**파일: `build.gradle` (관련 섹션)**

```gradle
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-starter'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

## 3. 애플리케이션 실행 방법

Spring Boot 애플리케이션을 실행하려면 터미널에서 프로젝트의 루트 디렉토리로 이동하여 다음 명령을 실행합니다:

```bash
./gradlew bootRun
```

애플리케이션이 시작되면 Spring Boot가 특정 포트(기본값은 8080)에서 시작되었음을 나타내는 로그를 볼 수 있습니다.

## 4. API 엔드포인트 테스트 방법

애플리케이션이 실행 중이면 새 터미널 창에서 `curl`을 사용하거나 웹 브라우저에서 열어 "Hello, World!" API 엔드포인트를 테스트할 수 있습니다.

**`curl` 사용:**

```bash
curl http://localhost:8080/hello
```

**예상 출력:**

```
Hello, World!
```

**웹 브라우저 사용:**

웹 브라우저를 열고 다음 주소로 이동합니다:

```
http://localhost:8080/hello
```

**결과 (스크린샷):**

<img width="424" height="149" alt="image" src="https://github.com/user-attachments/assets/f255d537-89da-47f7-884b-3425ada38d8a" />


---

## 학습 내용

이 섹션에서는 구현된 코드와 관련된 핵심 개념들을 설명합니다.

### 어노테이션(Annotation)이란?

어노테이션은 자바 코드에 메타데이터(코드에 대한 정보)를 추가하는 방법입니다. 코드 자체의 동작에 직접적인 영향을 주지 않으면서, 컴파일러나 런타임 시에 특정 작업을 수행하거나 코드의 의미를 부여하는 데 사용됩니다. Spring 프레임워크는 어노테이션을 광범위하게 사용하여 개발자가 XML 설정 대신 코드로 기능을 정의할 수 있도록 돕습니다.

### `@RestController` 어노테이션

`@RestController`는 Spring 프레임워크에서 RESTful 웹 서비스의 컨트롤러를 정의할 때 사용되는 어노테이션입니다. 이 어노테이션은 두 가지 주요 어노테이션의 조합입니다:

1.  **`@Controller`**: 이 클래스가 웹 요청을 처리하는 컨트롤러임을 나타냅니다.
2.  **`@ResponseBody`**: 이 어노테이션이 붙은 메서드의 반환 값이 HTTP 응답 본문(body)으로 직접 전송됨을 의미합니다. 일반적으로 객체를 JSON/XML 형태로 변환하여 전송합니다. `HelloWorldController`의 경우, `String` 타입이 그대로 응답 본문이 됩니다.

`@RestController`를 사용하면 별도의 뷰(HTML 파일 등)를 반환하지 않고, 데이터(예: JSON, XML, Plain Text)를 직접 응답으로 반환하는 REST API를 쉽게 구현할 수 있습니다.

### `@GetMapping` 어노테이션

`@GetMapping`은 HTTP GET 요청을 특정 핸들러 메서드에 매핑하는 어노테이션입니다. 이는 `@RequestMapping(method = RequestMethod.GET)`의 단축형입니다.

*   `@GetMapping("/hello")`: 이 어노테이션은 `/hello` 경로로 들어오는 HTTP GET 요청을 `helloWorld()` 메서드가 처리하도록 지시합니다. 사용자가 웹 브라우저에서 `http://localhost:8080/hello`와 같이 해당 URL에 접속하면, Spring은 이 메서드를 호출하여 반환 값을 클라이언트에게 전송합니다.

### `build.gradle` 파일

`build.gradle`은 Gradle 빌드 자동화 도구에서 사용되는 빌드 스크립트 파일입니다. 이 파일은 프로젝트의 의존성 관리, 태스크 정의, 빌드 프로세스 구성 등 프로젝트를 빌드하고 관리하는 데 필요한 모든 정보를 담고 있습니다.

*   **의존성 관리**: `dependencies` 블록에서 프로젝트가 필요로 하는 외부 라이브러리(JAR 파일)들을 선언합니다. Gradle은 이 정보를 바탕으로 필요한 라이브러리를 자동으로 다운로드하고 빌드 경로에 포함시킵니다.

### `spring-boot-starter-web` 의존성 (Dependency)

`spring-boot-starter-web`은 Spring Boot에서 웹 애플리케이션(RESTful 서비스 포함)을 개발하기 위한 핵심 스타터(Starter) 의존성입니다. "스타터"는 개발에 필요한 공통적인 의존성들을 한데 묶어 제공하여, 개발자가 개별 의존성을 일일이 추가할 필요 없이 편리하게 기능을 사용할 수 있도록 합니다.

`spring-boot-starter-web`에는 다음과 같은 주요 의존성들이 포함되어 있습니다:

*   **Spring MVC**: 웹 애플리케이션 개발을 위한 프레임워크.
*   **Tomcat**: 내장형 웹 서버. 별도의 서버 설치 없이 Spring Boot 애플리케이션을 단독으로 실행할 수 있게 해줍니다.
*   **Jackson**: JSON 데이터를 처리하기 위한 라이브러리.
*   **Validation**: 데이터 유효성 검사를 위한 라이브러리.

이 의존성을 추가함으로써, `@RestController`, `@GetMapping`과 같은 웹 관련 어노테이션과 내장 톰캣 서버를 사용하여 즉시 웹 서비스를 개발하고 실행할 수 있게 됩니다.

### 엔드포인트(Endpoint)란?

소프트웨어 시스템에서 "엔드포인트"는 통신이 이루어지는 지점을 의미합니다. REST API의 맥락에서는 클라이언트가 서버의 특정 자원(resource)에 접근하기 위해 사용하는 URL 경로를 지칭합니다.

*   예를 들어, `http://localhost:8080/hello`에서 `/hello`가 엔드포인트입니다. 클라이언트(웹 브라우저, `curl` 등)는 이 엔드포인트로 HTTP 요청을 보내고, 서버는 해당 요청을 처리하여 응답을 반환합니다. 각 엔드포인트는 특정 기능을 수행하며, HTTP 메서드(GET, POST, PUT, DELETE 등)에 따라 다른 작업을 수행할 수 있습니다.
