# 스프링 트랜잭션 관리 적용하기

이 문서는 Spring 입문 미션(`mission-01-spring-intro`)의 다섯 번째 테스크(`task-05-tx`)에서 **@Transactional**을 사용해 데이터 저장 시 트랜잭션을 적용하고, 예외 발생 시 롤백 동작을 확인한 과정을 정리합니다.

## 1. 구성 개요

- **패키지 구조**: `com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx`
- **주요 클래스**:
  - 서비스: `MemberTxService` (`@Transactional`로 트랜잭션 경계 설정)
  - 컨트롤러: `MemberTxController` (테스트용 API 제공)
  - 예외: `TxSimulationException` (`@ResponseStatus(INTERNAL_SERVER_ERROR)`로 매핑)
- **데이터 소스**: H2 인메모리 DB (`application.properties` 설정 재사용)
- **엔드포인트 베이스 경로**: `/mission01/task05/members`

## 2. 주요 구현

### 트랜잭션 서비스
- 파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/service/MemberTxService.java`
- 클래스 레벨 `@Transactional`로 모든 퍼블릭 메서드에 트랜잭션 적용.
- `create`: 정상 저장 → 커밋.
- `createWithFailure`: 저장 후 `TxSimulationException` 던짐 → 런타임 예외로 전체 트랜잭션 롤백.
- 조회는 `@Transactional(readOnly = true)`로 최적화.

### 예외
- 파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/exception/TxSimulationException.java`
- `@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)`로 매핑하여 클라이언트에 500 상태 전달.

### 컨트롤러
- 파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task05_tx/controller/MemberTxController.java`
- 엔드포인트
  - `POST /mission01/task05/members` : 정상 저장 (201 Location 헤더 포함)
  - `POST /mission01/task05/members/fail` : 예외 발생 → 500 반환, 데이터는 롤백
  - `GET /mission01/task05/members` : 전체 조회
  - `GET /mission01/task05/members/{id}` : 단건 조회

## 3. 실행 및 테스트

### 애플리케이션 실행
```bash
./gradlew bootRun
```

### API 호출 예시
1) 정상 커밋  
```bash
curl -i -X POST \
  -H 'Content-Type: application/json' \
  -d '{"name":"tx-user","email":"tx@example.com"}' \
  http://localhost:8080/mission01/task05/members
```

2) 예외로 롤백  
```bash
curl -i -X POST \
  -H 'Content-Type: application/json' \
  -d '{"name":"tx-fail","email":"fail@example.com"}' \
  http://localhost:8080/mission01/task05/members/fail
```
- 응답 상태: 500  
- 이후 `GET /mission01/task05/members` 결과에 저장된 데이터가 없어야 롤백이 확인됩니다.

### 테스트 실행
```bash
./gradlew test --tests "*task05_tx*"
```
(네트워크/권한 문제로 Gradle 다운로드가 불가능한 환경에서는 실패할 수 있습니다. 로컬에서 Gradle 캐시가 있는 경우 정상 실행됩니다.)

## 4. 학습 내용
- **@Transactional 기본 동작**: 런타임 예외 발생 시 트랜잭션이 자동 롤백됩니다. 체크 예외는 기본적으로 커밋되므로 필요 시 rollbackFor 옵션을 사용합니다.
- **트랜잭션 경계**: 비즈니스 로직이 있는 서비스 레이어에 경계를 두어 컨트롤러/리포지토리와 구분합니다.
- **읽기 전용 트랜잭션**: `readOnly = true`는 플러시를 생략하고 조회 성능을 약간 높여줍니다.
- **예외 → HTTP 상태 매핑**: `@ResponseStatus`를 사용해 도메인 예외를 HTTP 응답 코드로 명확히 매핑할 수 있습니다.
- **롤백 검증 패턴**: “저장 후 즉시 예외 발생” 패턴으로 실제 DB에 커밋되지 않는지 확인할 수 있습니다.
