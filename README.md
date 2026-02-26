# spring-missions-playground

Spring Framework & Spring Boot 학습을 위한 **미션 기반 실습 저장소**입니다.
입문부터 핵심 원리, MVC, DB, AOP 고급 주제까지 **Spring 관련 모든 미션을 하나의 레포지토리에서 단계적으로 정리하고 기록**하는 것을 목표로 합니다.

각 미션은 **실습 코드 + 문서(README)** 형태로 관리하며
학습 흐름을 따라가며 **개념 → 코드 → 정리**를 반복하는 방식으로 구성되어 있습니다.

---

## 학습 목표

* Spring Boot의 핵심 개념과 동작 원리 이해
* 객체 지향 설계 원칙과 DI / IoC / AOP 체득
* MVC, DB, 트랜잭션, AOP를 실제 코드로 구현
* 미션 단위로 학습 내용을 문서화하여 재사용 가능한 지식으로 정리

---

## 미션 목록 및 진행 현황

아래는 현재 이 레포지토리에서 다루는 Spring 미션 목록입니다.
각 미션은 토글을 열어 **하위 태스크 단위**로 확인할 수 있습니다.

---

<details>
<summary><strong>[백엔드] 스프링 입문</strong></summary>

스프링 부트의 기본 개념과 핵심 기능을 학습합니다.
REST API 구현, MVC 패턴, DI, JPA, 트랜잭션, AOP 등 스프링 부트의 주요 특징을 실습을 통해 익힙니다.

### 하위 테스크

* [x] **[간단한 웹 페이지와 REST API 엔드포인트 구현](docs/mission-01-spring-intro/task-01-hello/README.md)**
* [x] **[MVC 패턴의 이해와 기본 구조 설계](docs/mission-01-spring-intro/task-02-mvc/README.md)**
* [x] **[의존성 주입(DI) 이해하기](docs/mission-01-spring-intro/task-03-di/README.md)**
* [x] **[JPA와 스프링 데이터의 기본 CRUD 구현](docs/mission-01-spring-intro/task-04-jpa/README.md)**
* [x] **[트랜잭션 관리 설정하기](docs/mission-01-spring-intro/task-05-tx/README.md)**
* [x] **[AOP 적용하여 로깅 기능 추가하기](docs/mission-01-spring-intro/task-06-aop/README.md)**

</details>

---

<details>
<summary><strong>[백엔드] 스프링 핵심 원리 - 기본</strong></summary>

스프링 프레임워크의 핵심 원리와 객체 지향 설계를 심층적으로 학습합니다.
DI, IoC, AOP의 동작 원리와 스프링 컨테이너, 빈 생명주기, 스코프 개념을 실습을 통해 이해합니다.

### 하위 테스크

* [ ] 객체 지향 설계 원칙 이해하기
* [ ] Spring을 통한 객체 지향 원리 적용 실습
* [ ] 스프링 부트를 사용하여 웹 애플리케이션 프로젝트 생성하기
* [x] **[애너테이션을 사용하여 빈 주입하기](docs/mission-02-spring-core-basic/task-01-annotation-injection/README.md)**
* [ ] 인터페이스를 사용하여 의존성 주입하기
* [x] **[의존관계 자동 주입 방식 실습(@Autowired, @Qualifier, @Primary)](docs/mission-02-spring-core-basic/task-04-auto-injection/README.md)**
* [ ] 싱글톤 컨테이너 원리 이해하기
* [ ] 싱글톤 빈 스코프와 프로토타입 빈 스코프 구현하기
* [ ] 스프링 웹 스코프를 활용하여 빈 생성하기
* [x] **[순환 의존성 해결하기](docs/mission-02-spring-core-basic/task-02-circular-dependency/README.md)**
* [ ] AOP를 사용하여 애스펙트 구현하기
* [x] **[스프링 MVC를 이용하여 간단한 웹 페이지 구현하기](docs/mission-02-spring-core-basic/task-05-spring-mvc-web-page/README.md)**
* [ ] 스프링 부트와 스프링 MVC를 활용하여 웹 애플리케이션 개발하기
* [x] **[의존성 주입과 테스트를 위한 Mock 객체 사용](docs/mission-02-spring-core-basic/task-03-mock-object/README.md)**
* [ ] 스프링 핵심 원리를 활용한 간단한 예제 만들기

</details>

---

<details>
<summary><strong>[백엔드] HTTP 웹 기본 지식</strong></summary>

HTTP와 웹 통신의 기본 개념을 심층적으로 학습합니다.
요청-응답 구조, 메서드, 상태 코드, 헤더, 캐시, HTTP 버전별 차이를 이해합니다.

### 하위 테스크

* [ ] 네트워크 계층 이해하기
* [ ] URI의 구조 분석하기
* [ ] HTTP 요청과 응답 흐름 이해하기
* [ ] HTTP 메서드별 특징 분석
* [ ] HTTP 메서드의 실용적 활용
* [ ] HTTP 상태 코드 심화 이해하기
* [ ] 일반 HTTP 헤더와 보안 헤더 분석
* [ ] HTTP 요청 헤더와 응답 헤더의 역할 분석
* [ ] HTTP 캐시의 동작 원리
* [ ] HTTP/2와 HTTP/3의 개선점 비교

</details>

---

<details>
<summary><strong>[백엔드] 스프링 MVC</strong></summary>

스프링 MVC의 요청-응답 흐름과 주요 기능을 실습을 통해 학습합니다.
컨트롤러 매핑, 모델/뷰 분리, 검증, 예외 처리, 필터/인터셉터 등을 직접 구현합니다.

### 하위 테스크

* [ ] 스프링 MVC의 요청-응답 흐름 이해
* [ ] DispatcherServlet의 동작 방식 요약
* [ ] 애노테이션을 통한 요청 매핑
* [ ] Model과 View 분리하기
* [ ] View Resolver 설정과 활용
* [ ] 뷰 템플릿을 사용한 데이터 출력
* [ ] 타입 변환기를 통한 사용자 입력 데이터 변환
* [ ] 검증 애노테이션 사용
* [ ] 메시지 소스를 통한 다국어 지원 설정
* [ ] 예외 처리와 사용자 알림
* [ ] 필터와 인터셉터를 통한 요청 로깅

</details>

---

<details>
<summary><strong>[백엔드] 스프링 DB</strong></summary>

스프링의 데이터 접근 기술과 트랜잭션 관리를 학습합니다.
JdbcTemplate, JPA, MyBatis, Spring Data JPA를 실습하며 실무적인 데이터 처리 경험을 쌓습니다.

### 하위 테스크

* [ ] 스프링 JdbcTemplate을 사용한 데이터베이스 조회
* [ ] Spring Data JPA를 활용한 간단한 Repository 만들기
* [ ] Spring Boot에서 JPA 사용하여 데이터 CRUD 구현하기
* [ ] 트랜잭션 관리 이해 및 적용
* [ ] 트랜잭션 롤백을 위한 커스텀 예외 정의
* [ ] 트랜잭션 Isolation Level 설정 테스트
* [ ] 트랜잭션과 Lazy Loading 연관 테스트
* [ ] Spring Boot에서 MyBatis와 JPA를 동시에 사용하기
* [ ] 예외 처리를 통한 데이터 접근 안정성 강화

</details>

---

<details>
<summary><strong>[백엔드] 스프링 핵심 원리 - 고급</strong></summary>

스프링의 고급 개념과 디자인 패턴을 학습합니다.
프록시, 동적 프록시, AOP, ThreadLocal을 활용한 동시성 및 성능 최적화를 다룹니다.

### 하위 테스크

* [ ] 스프링 AOP 개념 정리
* [ ] @Aspect 어노테이션을 활용한 로깅 시스템 구현
* [ ] 스프링 AOP를 사용한 예외 처리 시스템 구현
* [ ] AOP 적용 시 성능 최적화 시스템 구현
* [ ] 템플릿 메서드 패턴을 활용한 확장 가능한 시스템 설계
* [ ] 프록시 패턴을 활용한 보안 검증 시스템 구축해보기
* [ ] 동적 프록시를 이용한 트랜잭션 관리 시스템 구현
* [ ] 쓰레드 로컬을 사용한 데이터베이스 연결 관리

</details>

---

## 기술 스택

* Java (JDK 25)
* Spring Boot 4.0.2
* Gradle
* Spring Web

---

## 실행 방법

```bash
./gradlew bootRun
```

애플리케이션 실행 후 기본 접속 주소:

```
http://localhost:8080
```

---

## 문서 구조 안내

각 미션 및 태스크별 학습 내용은 다음 구조로 관리합니다.

```
docs/
 └─ mission-xx-...
    └─ task-xx-...
       └─ README.md
```

* 코드 구현 배경
* 핵심 개념 정리
* 실행 결과 및 캡처
* 느낀 점 / 정리

을 중심으로 작성합니다.
