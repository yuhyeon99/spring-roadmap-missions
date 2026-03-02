# 스프링 핵심 원리 - 기본: 싱글톤 컨테이너 원리 이해하기

이 문서는 `mission-02-spring-core-basic`의 `task-10-singleton-container` 작업 내용을 정리한 보고서입니다.  
스프링 컨테이너의 기본 싱글톤 동작을 실습하고, 동일한 빈 조회 시 같은 인스턴스가 재사용되는지 확인했습니다.

## 1. 작업 개요

- 패키지: `com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container`
- 목표:
  - 동일 타입 빈을 컨테이너에서 여러 번 조회해도 같은 인스턴스인지 검증한다.
  - 싱글톤 빈 상태(`callCount`)가 공유되는지 확인한다.
  - 결과 확인 절차와 싱글톤 장단점을 정리한다.
- 엔드포인트:
  - `GET /mission02/task10/singleton/inspect`

## 2. 구현 단계와 주요 코드

### 2.1 싱글톤 동작 추적 빈 작성

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/singleton/SingletonTraceBean.java`

구현 내용:
- `instanceId`를 생성 시 1회 발급해 인스턴스 식별에 사용
- `touch()` 호출 시 `callCount`를 증가시켜 상태 공유 여부 확인

### 2.2 컨테이너 조회 서비스 구현

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/service/SingletonContainerService.java`

핵심 흐름:
- `ApplicationContext#getBean(SingletonTraceBean.class)`를 2회 호출
- 두 조회 결과의 `instanceId`, `identityHashCode`, 참조 동일성(`==`)을 비교
- `touch()` 2회 호출 후 `callCount` 증가 결과를 응답으로 반환

### 2.3 확인용 API 구성

- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/controller/SingletonContainerController.java`
- `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task10_singleton_container/dto/SingletonCheckResponse.java`

컨트롤러에서 `GET /inspect` 요청 시 싱글톤 검증 결과를 JSON으로 제공합니다.

## 3. 실행·빌드·테스트 방법과 예상 결과

### 3.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3.2 API 호출 예시

```bash
curl "http://localhost:8080/mission02/task10/singleton/inspect"
```

예상 응답(요약):

```json
{
  "firstLookupInstanceId": "동일 UUID",
  "secondLookupInstanceId": "동일 UUID",
  "firstLookupIdentityHash": 12345678,
  "secondLookupIdentityHash": 12345678,
  "sameInstance": true,
  "firstCallCount": 1,
  "secondCallCount": 2
}
```

검증 포인트:
- `sameInstance = true`
- `firstLookupInstanceId == secondLookupInstanceId`
- `secondCallCount = firstCallCount + 1`

### 3.3 테스트 실행

```bash
./gradlew test --tests "*SingletonContainerServiceTest"
```

예상 결과:
- 테스트 2건 성공
- 동일 빈 재조회 시 동일 객체 참조 및 공유 상태 검증 완료

## 4. 결과 확인 및 스크린샷

### 4.1 스크린샷 촬영 절차 (macOS 기준)

1. 터미널 1에서 서버 실행

```bash
./gradlew bootRun
```

2. 터미널 2에서 API 호출

```bash
curl "http://localhost:8080/mission02/task10/singleton/inspect"
```

3. 결과 영역 캡처
- 영역 캡처: `Shift + Command + 4`
- 창 단위 캡처: `Shift + Command + 4` 후 `Space`

4. 저장 권장 경로/파일명
- 경로: `docs/mission-02-spring-core-basic/task-10-singleton-container/`
- 파일명: `singleton-run-result.png`

## 5. 싱글톤 장단점 정리

장점:
- 객체를 한 번만 생성해 재사용하므로 메모리 사용량을 줄일 수 있습니다.
- 생성 비용이 큰 객체를 반복 생성하지 않아 성능에 유리합니다.
- 애플리케이션 전역에서 같은 인스턴스를 공유해 일관된 동작을 유지하기 쉽습니다.

단점:
- 상태를 가지는 필드를 두면 동시성 이슈가 발생할 수 있어 주의가 필요합니다.
- 전역 공유 특성 때문에 테스트 격리(독립성)가 어려워질 수 있습니다.
- 구조가 잘못되면 강한 결합으로 이어져 변경 비용이 커질 수 있습니다.

## 6. 학습 내용

- 스프링 컨테이너의 기본 빈 스코프는 싱글톤이므로, 같은 타입 빈 조회는 같은 객체를 반환합니다.
- 단순히 “동일 참조인지”뿐 아니라, `callCount`처럼 상태가 누적되는지도 함께 확인하면 싱글톤 특성을 더 명확히 이해할 수 있습니다.
- 실무에서는 싱글톤 빈을 가능한 무상태(stateless)로 설계해 동시성 문제를 예방하는 것이 중요합니다.
