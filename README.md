# spring-boot-intro-project

## 프로젝트 소개

이 프로젝트는 Spring Boot 백엔드 개발의 기본적인 개념과 실습을 학습하기 위한 저장소입니다. AOP 로깅, 트랜잭션 관리, JPA/Spring Data를 활용한 CRUD 구현, 의존성 주입(DI), MVC 패턴 이해, 간단한 웹 페이지 및 REST API 엔드포인트 구현 등 핵심 주제들을 다룹니다. Spring Boot를 처음 시작하거나 주요 기능을 연습하기 위한 repository 입니다.

## 주요 학습 내용 및 진행 상황

현재 프로젝트에서 다루는 주요 학습 내용과 진행 상황은 다음과 같습니다.

*   **[간단한 웹 페이지와 REST API 엔드포인트 구현](docs/api_setup.md)** (완료)
    *   "Hello, World!" 메시지를 반환하는 간단한 REST API (`/hello`)를 구현했습니다.
*   **의존성 주입(DI) 이해하기** ((예정))
*   **MVC 패턴의 이해와 기본 구조 설계** ((예정))
*   **JPA와 스프링 데이터의 기본 CRUD 구현** ((예정))
*   **트랜잭션 관리 설정하기** ((예정))
*   **AOP 적용하여 로깅 기능 추가하기** ((예정))

## 기술 스택

*   Java (JDK 25)
*   Spring Boot 4.0.2
*   Gradle
*   Spring Web

## 시작하기

### 전제 조건

프로젝트를 실행하려면 다음이 설치되어 있어야 합니다:

*   Java Development Kit (JDK) 25 이상
*   Gradle (Gradle Wrapper가 포함되어 있어 별도 설치 불필요)

### 애플리케이션 실행

프로젝트 루트 디렉토리에서 다음 Gradle 명령어를 실행하여 Spring Boot 애플리케이션을 시작합니다.

```bash
./gradlew bootRun
```

애플리케이션이 성공적으로 시작되면 터미널에 로그가 출력되며, 기본적으로 `http://localhost:8080` 포트에서 서비스가 시작됩니다.

## API 엔드포인트 사용

애플리케이션이 실행 중인 상태에서 다음 엔드포인트를 테스트할 수 있습니다.

### "Hello, World!" API

*   **URL**: `http://localhost:8080/hello`
*   **메서드**: `GET`
*   **설명**: 간단한 "Hello, World!" 메시지를 반환합니다.
*   **응답 예시**:
    ```
    Hello, World!
    ```

**테스트 방법:**

1.  **웹 브라우저**: `http://localhost:8080/hello` 에 접속합니다.
2.  **`curl` 명령어**: 터미널에서 다음 명령어를 실행합니다.
    ```bash
    curl http://localhost:8080/hello
    ```

## 학습 자료

각 학습 주제에 대한 더 자세한 설명과 코드 구현 과정은 `docs/` 디렉토리에 있는 Markdown 파일들을 참조해주세요.

*   [`docs/api_setup.md`](docs/api_setup.md): 간단한 웹 페이지와 REST API 엔드포인트 구현에 대한 자세한 설명과 핵심 개념(어노테이션, `build.gradle`, 의존성 등)을 학습할 수 있습니다.