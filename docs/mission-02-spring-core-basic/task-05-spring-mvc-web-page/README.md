# 스프링 핵심 원리 - 기본: 스프링 MVC를 이용하여 간단한 웹 페이지 구현하기

이 문서는 `mission-02-spring-core-basic`의 `task-05-spring-mvc-web-page` 작업 내용을 정리한 보고서입니다.  
Spring MVC의 핵심 구성 요소인 Controller, Model, View(Thymeleaf)를 사용해 간단한 동적 웹 페이지를 구현하고 동작을 확인했습니다.

## 1. 작업 개요

- 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task05_spring_mvc_web_page`
- 목표:
  - `@Controller`로 요청을 받아 뷰 이름을 반환한다.
  - `Model`에 데이터를 담아 뷰 템플릿으로 전달한다.
  - `@ModelAttribute`로 폼 입력을 객체로 바인딩해 다시 렌더링한다.
- 시나리오:
  - `GET /mission02/task05/mvc`: 기본 페이지 렌더링
  - `POST /mission02/task05/mvc/preview`: 이름/주제를 입력받아 Model을 갱신하고 같은 뷰를 다시 렌더링

## 2. 구현 단계와 주요 코드

### 2.1 뷰 템플릿 의존성 추가

- `build.gradle`

`spring-boot-starter-thymeleaf`를 추가해 서버 사이드 템플릿 렌더링을 가능하게 구성했습니다.

### 2.2 Controller 구현

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/controller/SimpleMvcPageController.java`

핵심 포인트:
- `@Controller` + `@RequestMapping("/mission02/task05/mvc")`
- `GET` 요청에서 기본 모델 데이터 구성 후 `mission02/task05/home` 뷰 반환
- `POST /preview` 요청에서 `@ModelAttribute LearningRequest`로 폼 데이터 바인딩
- 공백 입력은 기본값(`학습자`, `Spring MVC`)으로 보정

### 2.3 Model 데이터 생성 서비스 분리

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/service/MvcPageContentService.java`

페이지 문구/체크리스트 생성을 서비스로 분리해 Controller는 요청 흐름 제어에 집중하도록 구성했습니다.

### 2.4 폼 바인딩용 모델 객체

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/dto/LearningRequest.java`

폼 필드(`name`, `topic`)를 객체로 받아 `@ModelAttribute` 바인딩에 사용했습니다.

### 2.5 View(Thymeleaf) 구현

- `src/main/resources/templates/mission02/task05/home.html`

핵심 포인트:
- `th:text`로 Model 값을 HTML에 출력
- `th:each`로 학습 체크리스트 목록 렌더링
- `th:object` + `th:field`로 폼 데이터 바인딩
- 모바일/데스크톱 반응형 레이아웃 적용

### 2.6 웹 계층 테스트 작성

- `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task05_spring_mvc_web_page/SimpleMvcPageControllerTest.java`

검증 내용:
- GET 요청 시 뷰 이름, 모델 속성, 체크리스트 개수 확인
- POST 요청 시 폼 입력이 모델로 반영되는지 확인

## 3. 실행·빌드·테스트 방법과 예상 결과

### 3.1 애플리케이션 실행

```bash
./gradlew bootRun
```

실행 후 접속:

```
http://localhost:8080/mission02/task05/mvc
```

예상 결과:
- 환영 문구, 서버 렌더링 시각, 학습 체크리스트가 표시됨
- 폼 제출 시 입력값이 반영되어 동일 페이지가 다시 렌더링됨

### 3.2 테스트 실행

```bash
./gradlew test --tests "*SimpleMvcPageControllerTest"
```

예상 결과:
- 테스트 2건 성공
- Controller-Model-View 연결 및 폼 바인딩 동작 검증 완료

## 4. 결과 확인 방법

- 브라우저에서 `GET /mission02/task05/mvc` 접속
- 폼에 이름/주제를 입력하고 제출한 뒤, 화면 문구가 바뀌는지 확인
- 테스트 리포트에서 `SimpleMvcPageControllerTest` 성공 여부 확인
- 필요 시 브라우저 화면 캡처를 보고서/제출 자료에 첨부

## 학습 내용

- Spring MVC에서 Controller는 요청을 받아 비즈니스/가공 로직과 뷰 렌더링을 연결하는 진입점입니다.
- Model은 뷰에서 사용할 데이터를 담는 컨테이너이며, 템플릿 엔진은 이 값을 읽어 최종 HTML을 생성합니다.
- View 템플릿(Thymeleaf)은 서버에서 렌더링되기 때문에, 클라이언트 자바스크립트 없이도 동적 페이지를 만들 수 있습니다.
- `@ModelAttribute`를 사용하면 HTTP 요청 파라미터를 자바 객체로 매핑할 수 있어 폼 처리 코드가 단순해집니다.
- Controller가 모든 문자열/목록 생성 책임을 가지면 복잡해지므로, `MvcPageContentService`처럼 역할을 분리하면 유지보수가 쉬워집니다.
