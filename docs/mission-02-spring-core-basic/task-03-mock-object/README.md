# 스프링 핵심 원리 - 기본: 의존성 주입과 테스트를 위한 Mock 객체 사용

이 문서는 `mission-02-spring-core-basic`의 `task-03-mock-object` 작업 내용을 정리한 보고서입니다.  
실제 데이터베이스 없이 `Mock` 객체를 의존성으로 주입해 CRUD 테스트를 수행하고, 서비스 계층의 동작을 검증했습니다.

## 1. 작업 개요

- 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object`
- 목표:
  - 서비스가 구체 구현이 아닌 `Repository` 인터페이스에 의존하도록 구성한다.
  - 테스트에서 `Mock Repository`를 주입해 DB 없이 CRUD 시나리오를 검증한다.
  - 생성/조회/목록/수정/삭제 및 예외 케이스를 테스트 코드로 확인한다.
- 시나리오:
  - `MockMemberService`는 생성자 주입으로 `MockMemberRepository`를 받는다.
  - 테스트에서는 Mockito의 `@Mock`, `@InjectMocks`를 사용해 가짜 저장소를 주입한다.

## 2. 구현 단계와 주요 코드

### 2.1 도메인 및 저장소 추상화

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/domain/MockMember.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/repository/MockMemberRepository.java`

`MockMember` 도메인 객체와 CRUD 계약 인터페이스(`MockMemberRepository`)를 분리해,  
서비스가 저장소 구현 세부사항(DB, 파일, 메모리 등)에 직접 의존하지 않도록 구성했습니다.

### 2.2 생성자 주입 기반 서비스 구성

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/service/MockMemberService.java`

`MockMemberService`는 생성자 주입으로 `MockMemberRepository`를 받아 CRUD 로직을 수행합니다.

- `createMember`: 신규 회원 생성 후 저장
- `findMember`: ID 조회, 미존재 시 예외
- `listMembers`: 전체 목록 조회
- `updateMember`: 기존 회원 조회 후 수정 저장
- `deleteMember`: 존재 여부 확인 후 삭제

### 2.3 테스트 환경용 Mock 주입 및 CRUD 검증

- `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task03_mock_object/MockMemberServiceTest.java`

Mockito를 사용해 저장소를 Mock으로 대체했습니다.

- `@Mock MockMemberRepository`: 가짜 저장소 생성
- `@InjectMocks MockMemberService`: Mock 저장소를 서비스에 주입

테스트 케이스:
- 생성: `save` 스텁으로 ID 부여 후 생성 결과 검증
- 조회: `findById` 스텁으로 가짜 데이터 반환 검증
- 목록: `findAll` 스텁 목록 반환 검증
- 수정: `findById` + `save` 스텁으로 갱신 결과 검증
- 삭제: `existsById`/`deleteById` 호출 검증
- 예외: 삭제 대상 미존재 시 예외 검증

## 3. 실행·빌드·테스트 방법과 예상 결과

### 3.1 테스트 실행

```bash
./gradlew test --tests "*MockMemberServiceTest"
```

예상 결과:
- 테스트 6건 성공
- 실제 DB 연결 없이 Mock 데이터로 CRUD 흐름 검증 완료

### 3.2 전체 테스트 실행(선택)

```bash
./gradlew test
```

## 4. 결과 확인 방법

- Gradle 테스트 로그에서 `MockMemberServiceTest`의 6개 테스트가 모두 `PASSED`인지 확인
- 테스트 리포트(`build/reports/tests/test/index.html`)에서 케이스별 성공 여부 확인
- 코드에서 `@InjectMocks`와 `@Mock` 조합으로 의존성이 대체 주입되는지 확인

## 학습 내용

- 의존성 주입의 핵심은 구현체가 아니라 인터페이스에 의존하도록 설계하는 것입니다. 이렇게 하면 런타임에는 실제 구현체를, 테스트에서는 Mock 구현을 손쉽게 교체할 수 있습니다.
- Mock 객체는 테스트 대상을 외부 요인(DB, 네트워크, 파일 시스템)에서 분리해, 서비스 로직만 빠르고 안정적으로 검증하게 도와줍니다.
- CRUD 테스트에서 각 저장소 메서드의 반환값을 명시적으로 스텁하면, 입력과 출력 관계를 명확히 추적할 수 있습니다.
- 예외 케이스(존재하지 않는 ID 삭제 등)도 함께 검증해야 서비스 동작 경계를 명확히 정의할 수 있습니다.
