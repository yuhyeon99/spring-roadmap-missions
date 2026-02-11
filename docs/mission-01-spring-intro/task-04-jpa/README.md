# JPA와 스프링 데이터 JPA로 CRUD 구현하기

이 문서는 Spring 입문 미션(`mission-01-spring-intro`)의 네 번째 테스크(`task-04-jpa`)에서 **Spring Data JPA**를 사용해 회원 엔티티의 CRUD 기능을 구현한 과정을 정리합니다. H2 인메모리 DB를 사용해 애플리케이션 실행 시 자동으로 테이블을 생성·삭제하도록 설정했습니다.

## 1. 구성 개요

- **패키지 구조**: `com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa` 아래에 `domain`, `repository`, `service`, `controller`, `dto`로 분리
- **기술 스택**: Spring Data JPA, H2 in-memory DB, Spring Boot 4.0.2
- **엔티티**: `Member` (`id`, `name`, `email`)
- **엔드포인트 베이스 경로**: `/mission01/task04/members`

## 2. 주요 구현

### 엔티티
- 파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/domain/Member.java`
- `@Entity`, `@Table(name = "members")`로 매핑하고, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`로 PK를 자동 증가시킵니다.

### 리포지토리
- 파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/repository/MemberJpaRepository.java`
- `JpaRepository<Member, Long>` 상속만으로 기본 CRUD 메서드(`save`, `findAll`, `findById`, `deleteById` 등)를 제공합니다.

### 서비스
- 파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/service/MemberJpaService.java`
- 클래스 레벨 `@Transactional`로 트랜잭션을 관리합니다.
- 기능: 생성(`create`), 단건 조회(`findById`), 전체 조회(`findAll`), 수정(`update`), 삭제(`delete`).
- 존재하지 않는 ID 요청 시 `ResponseStatusException(HttpStatus.NOT_FOUND)`을 던져 404를 반환합니다.

### 컨트롤러
- 파일: `src/main/java/com/goorm/springmissionsplayground/mission01_spring_intro/task04_jpa/controller/MemberJpaController.java`
- REST 엔드포인트
  - `POST /mission01/task04/members` : 회원 생성 (201 Created, Location 헤더 설정)
  - `GET /mission01/task04/members` : 전체 조회
  - `GET /mission01/task04/members/{id}` : 단건 조회
  - `PUT /mission01/task04/members/{id}` : 수정
  - `DELETE /mission01/task04/members/{id}` : 삭제 (204 No Content)

### DTO
- 파일: `dto/MemberRequest.java`, `dto/MemberResponse.java`
- 요청/응답 전용 클래스로 도메인 모델을 분리했습니다.

### 데이터베이스 및 JPA 설정
- 파일: `src/main/resources/application.properties`
- H2 인메모리 DB URL: `jdbc:h2:mem:mission01`
- `spring.jpa.hibernate.ddl-auto=create-drop` 로 실행 시 테이블 생성, 종료 시 삭제
- `spring.jpa.show-sql=true`, `hibernate.format_sql=true`로 SQL 로그 확인 가능
- 개발 편의를 위해 H2 콘솔 `/h2-console` 활성화

## 3. 실행 및 테스트

### 애플리케이션 실행
```bash
./gradlew bootRun
```

### API 호출 예시 (`/mission01/task04/members`)
1) 회원 생성  
```bash
curl -i -X POST \\
  -H \"Content-Type: application/json\" \\
  -d '{\"name\":\"JPA User\",\"email\":\"jpa@example.com\"}' \\
  http://localhost:8080/mission01/task04/members
```

2) 전체 조회  
```bash
curl http://localhost:8080/mission01/task04/members
```

3) 단건 조회  
```bash
curl http://localhost:8080/mission01/task04/members/1
```

4) 수정  
```bash
curl -X PUT \\
  -H \"Content-Type: application/json\" \\
  -d '{\"name\":\"Updated\",\"email\":\"updated@example.com\"}' \\
  http://localhost:8080/mission01/task04/members/1
```

5) 삭제  
```bash
curl -X DELETE http://localhost:8080/mission01/task04/members/1
```

### 테스트 실행
```bash
./gradlew test --tests \"*task04_jpa*\"
```

## 4. 학습 내용
- **Spring Data JPA**: `JpaRepository` 상속만으로 CRUD 메서드가 자동 제공됩니다. 복잡한 SQL을 작성하지 않아도 되고, 필요 시 메서드 이름 기반 쿼리 또는 @Query로 확장할 수 있습니다.
- **엔티티 매핑**: `@Entity` 클래스는 기본 생성자가 필요하며, 필드는 private으로 두고 getter를 제공합니다. ID 전략을 `IDENTITY`로 두면 DB가 PK를 생성합니다.
- **트랜잭션**: 서비스 레이어에 `@Transactional`을 적용하면 같은 트랜잭션 안에서 조회 후 변경(Dirty Checking)한 내용이 커밋 시 자동 반영됩니다. 읽기 전용 조회는 `@Transactional(readOnly = true)`로 최적화할 수 있습니다.
- **예외 처리**: 조회 결과가 없을 때 `ResponseStatusException`으로 404를 반환해 클라이언트가 명확히 실패 원인을 알 수 있습니다.
- **H2 인메모리 DB 활용**: 설정만으로 실행 시점에 깔끔한 DB가 준비되므로, 실습과 테스트를 빠르게 반복할 수 있습니다.
