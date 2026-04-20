# Spring Data JPA를 활용한 간단한 Repository 만들기

이 문서는 `mission-05-spring-db`의 `task-03-spring-data-jpa-repository` 구현 결과를 정리한 보고서입니다. Spring Data JPA의 Repository 인터페이스를 정의하고, 이를 통해 사용자 정보를 저장·조회·수정·삭제하는 간단한 예제를 REST API와 테스트 코드로 구성했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-05-spring-db` / `task-03-spring-data-jpa-repository`
- 목표:
  - `JpaRepository`를 상속하는 Repository 인터페이스를 만든다.
  - Spring Data JPA가 제공하는 기본 CRUD 메서드와 메서드 이름 기반 조회를 예제로 확인한다.
  - 사용자 정보 저장, 단건 조회, 목록 조회, 이메일 조회, 수정, 삭제 흐름을 실제 코드와 테스트로 검증한다.
- 베이스 경로: `/mission05/task03/users`
- 사용 기술: `Spring Boot`, `Spring Data JPA`, `Hibernate`, `H2 Database`, `MockMvc`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/controller/SpringDataJpaUserController.java` | 사용자 CRUD와 이메일 검색 API 엔드포인트를 제공합니다. |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/service/SpringDataJpaUserService.java` | 트랜잭션 경계를 관리하고 리포지토리 CRUD 호출을 조합합니다. |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/repository/SpringDataJpaUserRepository.java` | `JpaRepository`를 상속하고 `findByEmail`, `existsByEmail` 조회 메서드를 선언합니다. |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/domain/SpringDataJpaUser.java` | 사용자 엔터티와 테이블 매핑, 상태 변경 메서드를 정의합니다. |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/dto/SpringDataJpaUserRequest.java` | 생성/수정 요청 JSON을 바인딩하고 입력값을 검증합니다. |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/dto/SpringDataJpaUserResponse.java` | 엔터티를 API 응답 JSON 형태로 변환합니다. |
| Config | `src/main/resources/application.properties` | H2, Hibernate DDL 자동 생성, SQL 출력 등 공통 실행 환경을 설정합니다. |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/SpringDataJpaUserControllerTest.java` | Spring Data JPA Repository 기반 CRUD와 이메일 검색 흐름을 통합 테스트로 검증합니다. |
| Artifact | `docs/mission-05-spring-db/task-03-spring-data-jpa-repository/task03-gradle-test-output.txt` | `task03` 테스트 실행 결과를 저장한 콘솔 출력 파일입니다. |

## 3. 구현 단계와 주요 코드 해설

1. `SpringDataJpaUser` 엔터티를 만들고 `@Entity`, `@Table(name = "mission05_task03_users")`로 학습용 사용자 테이블에 매핑했습니다. 이메일은 `unique = true`로 두어 중복 저장을 막을 수 있게 했습니다.
2. `SpringDataJpaUserRepository`는 `JpaRepository<SpringDataJpaUser, Long>`를 상속하고, 추가로 `findByEmail`, `existsByEmail` 메서드를 선언했습니다. 구현 클래스는 직접 만들지 않고 Spring Data JPA가 런타임에 자동 생성합니다.
3. `SpringDataJpaUserService`는 클래스 레벨 `@Transactional`로 쓰기 작업의 기본 트랜잭션 경계를 두고, 조회 메서드는 `@Transactional(readOnly = true)`로 분리했습니다. 생성 시에는 `existsByEmail()`로 중복 여부를 빠르게 확인하고, 수정 시에는 `findByEmail()` 결과를 이용해 현재 사용자 외의 중복 이메일을 막습니다.
4. `SpringDataJpaUserController`는 `/mission05/task03/users` 아래에 `POST`, `GET`, `PUT`, `DELETE`를 구성하고, `GET /search?email=...` 엔드포인트로 메서드 이름 기반 조회 기능도 함께 드러내도록 했습니다.
5. `SpringDataJpaUserControllerTest`는 생성 → ID 조회 → 이메일 조회 → 목록 조회 → 수정 → 중복 이메일 생성 시도 → 삭제 → 삭제 후 404 확인 순서로 흐름을 검증합니다. 이 테스트 하나로 Repository 인터페이스가 실제 HTTP 요청 처리까지 연결되는 과정을 확인할 수 있습니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `SpringDataJpaUserController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/controller/SpringDataJpaUserController.java`
- 역할: 사용자 CRUD 및 이메일 검색 API 엔드포인트 제공
- 상세 설명:
- 기본 경로는 `/mission05/task03/users`입니다.
- `POST /mission05/task03/users`, `GET /mission05/task03/users`, `GET /mission05/task03/users/{id}`, `PUT /mission05/task03/users/{id}`, `DELETE /mission05/task03/users/{id}`를 제공합니다.
- `GET /mission05/task03/users/search?email=...`는 Repository의 `findByEmail()` 예제를 HTTP 요청으로 확인하기 위한 검색 엔드포인트입니다.

<details>
<summary><code>SpringDataJpaUserController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.controller;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.domain.SpringDataJpaUser;
import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.dto.SpringDataJpaUserRequest;
import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.dto.SpringDataJpaUserResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.service.SpringDataJpaUserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission05/task03/users")
public class SpringDataJpaUserController {

    private final SpringDataJpaUserService userService;

    public SpringDataJpaUserController(SpringDataJpaUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<SpringDataJpaUserResponse> create(@RequestBody @Valid SpringDataJpaUserRequest request) {
        SpringDataJpaUser created = userService.create(request.getName(), request.getEmail());
        return ResponseEntity
                .created(URI.create("/mission05/task03/users/" + created.getId()))
                .body(SpringDataJpaUserResponse.from(created));
    }

    @GetMapping
    public List<SpringDataJpaUserResponse> list() {
        return userService.findAll().stream()
                .map(SpringDataJpaUserResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public SpringDataJpaUserResponse get(@PathVariable Long id) {
        return SpringDataJpaUserResponse.from(userService.findById(id));
    }

    @GetMapping("/search")
    public SpringDataJpaUserResponse getByEmail(@RequestParam String email) {
        return SpringDataJpaUserResponse.from(userService.findByEmail(email));
    }

    @PutMapping("/{id}")
    public SpringDataJpaUserResponse update(
            @PathVariable Long id,
            @RequestBody @Valid SpringDataJpaUserRequest request
    ) {
        SpringDataJpaUser updated = userService.update(id, request.getName(), request.getEmail());
        return SpringDataJpaUserResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
```

</details>

### 4.2 `SpringDataJpaUserService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/service/SpringDataJpaUserService.java`
- 역할: 트랜잭션과 비즈니스 흐름 관리
- 상세 설명:
- 핵심 공개 메서드는 `create`, `findAll`, `findById`, `findByEmail`, `update`, `delete`입니다.
- 생성 시에는 `existsByEmail()`로 중복 이메일을 검사하고, 수정 시에는 `findByEmail()` 결과를 바탕으로 현재 사용자 외의 중복을 막습니다.
- 없는 사용자 ID 또는 이메일이 들어오면 `404 Not Found`, 중복 이메일이면 `409 Conflict`로 응답되도록 `ResponseStatusException`을 사용했습니다.

<details>
<summary><code>SpringDataJpaUserService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.domain.SpringDataJpaUser;
import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.repository.SpringDataJpaUserRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class SpringDataJpaUserService {

    private final SpringDataJpaUserRepository userRepository;

    public SpringDataJpaUserService(SpringDataJpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SpringDataJpaUser create(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        return userRepository.save(new SpringDataJpaUser(name, email));
    }

    @Transactional(readOnly = true)
    public List<SpringDataJpaUser> findAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional(readOnly = true)
    public SpringDataJpaUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public SpringDataJpaUser findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    public SpringDataJpaUser update(Long id, String name, String email) {
        SpringDataJpaUser user = findById(id);
        validateDuplicatedEmail(email, id);
        user.updateProfile(name, email);
        return user;
    }

    public void delete(Long id) {
        SpringDataJpaUser user = findById(id);
        userRepository.delete(user);
    }

    private void validateDuplicatedEmail(String email, Long currentUserId) {
        userRepository.findByEmail(email)
                .filter(existingUser -> !existingUser.getId().equals(currentUserId))
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
                });
    }
}
```

</details>

### 4.3 `SpringDataJpaUserRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/repository/SpringDataJpaUserRepository.java`
- 역할: Spring Data JPA Repository 인터페이스 정의
- 상세 설명:
- `JpaRepository<SpringDataJpaUser, Long>`를 상속하므로 `save`, `findById`, `findAll`, `delete` 같은 기본 CRUD를 바로 사용할 수 있습니다.
- `findByEmail(String email)`은 이메일 단건 조회 예제이고, `existsByEmail(String email)`은 중복 여부 확인 예제입니다.
- 구현 클래스를 작성하지 않아도 스프링 데이터 JPA가 런타임에 프록시 구현체를 자동으로 생성합니다.

<details>
<summary><code>SpringDataJpaUserRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.domain.SpringDataJpaUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaUserRepository extends JpaRepository<SpringDataJpaUser, Long> {

    Optional<SpringDataJpaUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
```

</details>

### 4.4 `SpringDataJpaUser.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/domain/SpringDataJpaUser.java`
- 역할: 사용자 엔터티와 테이블 매핑
- 상세 설명:
- `@Entity`, `@Table(name = "mission05_task03_users")`로 `task03` 전용 테이블과 매핑했습니다.
- `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`로 기본 키를 자동 생성합니다.
- 수정 흐름에서는 `updateProfile()`만 호출하고, 실제 UPDATE SQL 반영은 트랜잭션 종료 시점의 변경 감지가 처리합니다.

<details>
<summary><code>SpringDataJpaUser.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mission05_task03_users")
public class SpringDataJpaUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    protected SpringDataJpaUser() {
        // JPA 기본 생성자
    }

    public SpringDataJpaUser(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
```

</details>

### 4.5 `SpringDataJpaUserRequest.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/dto/SpringDataJpaUserRequest.java`
- 역할: 생성/수정 요청 DTO
- 상세 설명:
- 사용자 이름과 이메일을 JSON 요청 본문에서 바인딩합니다.
- `@NotBlank`, `@Size`, `@Email` 검증으로 빈 값과 잘못된 이메일 형식을 초기에 차단합니다.
- 같은 DTO를 생성과 수정에서 공통으로 사용해 입력 규칙을 한 곳에 모았습니다.

<details>
<summary><code>SpringDataJpaUserRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SpringDataJpaUserRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 30, message = "이름은 30자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

</details>

### 4.6 `SpringDataJpaUserResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/dto/SpringDataJpaUserResponse.java`
- 역할: 사용자 응답 DTO
- 상세 설명:
- 엔터티를 그대로 외부에 노출하지 않고 `id`, `name`, `email`만 응답으로 반환합니다.
- `from()` 정적 팩터리 메서드로 엔터티를 응답 DTO로 일관되게 변환합니다.
- 컨트롤러는 모든 응답에서 이 DTO를 사용해 응답 형식을 통일합니다.

<details>
<summary><code>SpringDataJpaUserResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.dto;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.domain.SpringDataJpaUser;

public class SpringDataJpaUserResponse {

    private final Long id;
    private final String name;
    private final String email;

    public SpringDataJpaUserResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static SpringDataJpaUserResponse from(SpringDataJpaUser user) {
        return new SpringDataJpaUserResponse(user.getId(), user.getName(), user.getEmail());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
```

</details>

### 4.7 `application.properties`

- 파일 경로: `src/main/resources/application.properties`
- 역할: H2와 JPA 공통 실행 설정
- 상세 설명:
- 인메모리 H2 데이터베이스와 Hibernate DDL 자동 생성 설정을 통해 별도 DB 설치 없이 실습할 수 있게 합니다.
- `spring.jpa.show-sql=true`, `hibernate.format_sql=true`로 실행 중 SQL을 콘솔에서 확인할 수 있습니다.
- `task03`도 이 공통 설정을 그대로 사용해 `mission05_task03_users` 테이블을 자동 생성합니다.

<details>
<summary><code>application.properties</code> 전체 코드</summary>

```properties
spring.application.name=core

# Mission04 Task02: Thymeleaf View Resolver 설정
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false

# H2 in-memory DB 설정 (테스트/학습용)
spring.datasource.url=jdbc:h2:mem:mission01;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA 설정
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 콘솔 (개발 편의를 위해 활성화)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

</details>

### 4.8 `SpringDataJpaUserControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission05_spring_db/task03_spring_data_jpa_repository/SpringDataJpaUserControllerTest.java`
- 역할: Repository 기반 CRUD와 이메일 검색 흐름 통합 검증
- 상세 설명:
- 검증 시나리오는 사용자 생성, ID 조회, 이메일 조회, 목록 조회, 수정, 중복 이메일 생성 시 409, 삭제, 삭제 후 404 확인 순서입니다.
- `WebApplicationContext`와 `MockMvc`를 사용해 컨트롤러, 서비스, 리포지토리, JPA 매핑이 실제로 함께 동작하는지 검증합니다.
- 정상 흐름과 예외 흐름(중복 이메일, 삭제 후 조회 실패)을 모두 보장합니다.

<details>
<summary><code>SpringDataJpaUserControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.repository.SpringDataJpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SpringDataJpaUserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SpringDataJpaUserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("Spring Data JPA Repository 기반 사용자 CRUD와 이메일 조회가 동작한다")
    void userCrudFlowWithEmailSearch() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/mission05/task03/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "박리포지토리",
                                  "email": "repository@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("박리포지토리"))
                .andExpect(jsonPath("$.email").value("repository@example.com"))
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");
        Long userId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mockMvc.perform(get("/mission05/task03/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("박리포지토리"))
                .andExpect(jsonPath("$.email").value("repository@example.com"));

        mockMvc.perform(get("/mission05/task03/users/search")
                        .param("email", "repository@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("repository@example.com"));

        mockMvc.perform(get("/mission05/task03/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId))
                .andExpect(jsonPath("$[0].name").value("박리포지토리"));

        mockMvc.perform(put("/mission05/task03/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "박리포지토리수정",
                                  "email": "repository-updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("박리포지토리수정"))
                .andExpect(jsonPath("$.email").value("repository-updated@example.com"));

        mockMvc.perform(post("/mission05/task03/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "중복사용자",
                                  "email": "repository-updated@example.com"
                                }
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/mission05/task03/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/mission05/task03/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
}
```

</details>

### 4.9 `task03-gradle-test-output.txt`

- 파일 경로: `docs/mission-05-spring-db/task-03-spring-data-jpa-repository/task03-gradle-test-output.txt`
- 역할: `task03` 테스트 실행 결과 보관
- 상세 설명:
- `./gradlew test --tests ...` 실행 결과를 그대로 저장한 파일입니다.
- `BUILD SUCCESSFUL` 문구와 `task03` 테스트가 실제로 수행된 흔적을 문서와 함께 보관합니다.
- 제출 시 이미지 대신 실행 결과를 재확인하는 보조 자료로 사용할 수 있습니다.

<details>
<summary><code>task03-gradle-test-output.txt</code> 내용</summary>

```text
> Task :compileJava
> Task :processResources UP-TO-DATE
> Task :classes
> Task :compileTestJava UP-TO-DATE
> Task :processTestResources NO-SOURCE
> Task :testClasses UP-TO-DATE
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-04-20T16:23:03.856+09:00  INFO 9922 --- [core] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
Hibernate:
    drop table if exists members cascade
Hibernate:
    drop table if exists mission05_task01_members cascade
Hibernate:
    drop table if exists mission05_task02_members cascade
Hibernate:
    drop table if exists mission05_task02_teams cascade
Hibernate:
    drop table if exists mission05_task03_users cascade
2026-04-20T16:23:03.859+09:00  INFO 9922 --- [core] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-04-20T16:23:03.860+09:00  INFO 9922 --- [core] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
> Task :test

BUILD SUCCESSFUL in 4s
4 actionable tasks: 2 executed, 2 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.0/userguide/configuration_cache_enabling.html
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 Spring Data JPA Repository 인터페이스

- 핵심:
- `JpaRepository`를 상속하는 인터페이스만 선언하면, 기본 CRUD 구현체를 직접 작성하지 않아도 됩니다.
- 스프링 데이터 JPA가 런타임에 프록시 객체를 만들어 `save`, `findById`, `findAll`, `delete` 같은 메서드를 처리합니다.
- 왜 쓰는가:
- 반복적인 DAO 구현 코드를 줄이고, 도메인별 데이터 접근 규칙을 인터페이스 중심으로 정리할 수 있습니다.
- 서비스 계층은 "무엇을 조회하고 저장할지"에 집중하고, 구체적인 JPA 처리 코드는 프레임워크에 맡길 수 있습니다.
- 참고 링크:
- Spring Data JPA Repositories: https://docs.spring.io/spring-data/jpa/reference/repositories.html

### 5.2 메서드 이름 기반 쿼리 생성

- 핵심:
- `findByEmail`, `existsByEmail`처럼 메서드 이름을 규칙에 맞게 선언하면 Spring Data JPA가 이를 해석해 쿼리를 만듭니다.
- 간단한 조건 조회는 `@Query` 없이도 Repository 인터페이스만으로 표현할 수 있습니다.
- 왜 쓰는가:
- 단순 조회 로직을 빠르게 추가할 수 있고, 인터페이스 이름만 읽어도 어떤 조회인지 의도를 파악하기 쉽습니다.
- 학습 단계에서는 SQL이나 JPQL을 직접 작성하기 전에 Repository 추상화가 무엇을 대신해 주는지 이해하기 좋습니다.
- 참고 링크:
- JPA Query Methods: https://docs.spring.io/spring-data/jpa/reference/4.1/jpa/query-methods.html
- Defining Query Methods: https://docs.spring.io/spring-data/jpa/reference/4.1/repositories/query-methods-details.html

### 5.3 `@Transactional`과 변경 감지

- 핵심:
- 트랜잭션 안에서 조회한 엔터티의 값을 변경하면, JPA가 트랜잭션 종료 시점에 변경 내용을 감지해 UPDATE SQL을 실행합니다.
- 조회 전용 메서드에는 `@Transactional(readOnly = true)`를 주어 의도를 분명히 하고 불필요한 쓰기 컨텍스트 사용을 줄일 수 있습니다.
- 왜 쓰는가:
- 수정 로직에서 매번 `save()`를 다시 호출하지 않아도 되어, 엔터티 상태 변경 책임을 도메인 객체에 더 자연스럽게 둘 수 있습니다.
- 서비스 계층이 트랜잭션 경계를 명확히 가지면 조회/수정 흐름을 예측하기 쉬워집니다.
- 참고 링크:
- Using `@Transactional`: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
- Jakarta Persistence `@Entity`: https://jakarta.ee/specifications/persistence/3.1/apidocs/jakarta.persistence/jakarta/persistence/entity

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

- 예상 결과:
- 애플리케이션이 `8080` 포트에서 실행됩니다.
- 콘솔에 Hibernate가 `mission05_task03_users` 테이블을 생성하는 SQL을 출력합니다.

### 6.2 API 호출 또는 화면 접근 방법

```bash
curl -i -X POST http://localhost:8080/mission05/task03/users \
  -H "Content-Type: application/json" \
  -d '{"name":"김리포지토리","email":"repository-user@example.com"}'
```

```bash
curl -i http://localhost:8080/mission05/task03/users
```

```bash
curl -i "http://localhost:8080/mission05/task03/users/search?email=repository-user@example.com"
```

```bash
curl -i -X PUT http://localhost:8080/mission05/task03/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"김리포지토리수정","email":"repository-user-updated@example.com"}'
```

```bash
curl -i -X DELETE http://localhost:8080/mission05/task03/users/1
```

- 예상 결과:
- 생성 요청은 `201 Created`와 `Location` 헤더를 반환합니다.
- 목록/단건/이메일 검색 요청은 저장된 사용자 정보를 JSON으로 반환합니다.
- 수정 요청은 변경된 이름과 이메일을 반환하고, 삭제 요청은 `204 No Content`를 반환합니다.

### 6.3 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.SpringDataJpaUserControllerTest
```

- 예상 결과:
- `SpringDataJpaUserControllerTest`가 통과합니다.
- 콘솔 마지막에 `BUILD SUCCESSFUL`이 출력됩니다.
- HTML 테스트 리포트는 `build/reports/tests/test/index.html`에서 확인할 수 있습니다.

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
- 생성 응답에 `201 Created`, `Location: /mission05/task03/users/{id}`가 포함되면 저장이 성공한 것입니다.
- `GET /mission05/task03/users/search?email=...` 호출에서 같은 사용자 정보가 반환되면 `findByEmail()` 메서드가 정상 동작한 것입니다.
- 동일 이메일로 두 번째 생성 요청을 보냈을 때 `409 Conflict`가 반환되면 중복 검증이 정상 동작한 것입니다.
- 삭제 후 같은 ID를 다시 조회했을 때 `404 Not Found`가 반환되면 삭제 흐름까지 정상입니다.
- 결과 확인 파일:
- 콘솔 실행 결과 파일명: `task03-gradle-test-output.txt`
- 저장 위치: `docs/mission-05-spring-db/task-03-spring-data-jpa-repository/task03-gradle-test-output.txt`
- 추가 확인 경로:
- HTML 테스트 리포트: `build/reports/tests/test/index.html`
- 스크린샷:
- 이번 태스크는 REST API 응답과 테스트 출력 중심이라 별도 이미지 스크린샷은 만들지 않았습니다.

## 8. 학습 내용

이번 태스크에서 핵심은 "리포지토리 구현 클래스를 직접 만들지 않아도 된다"는 점을 코드로 확인한 것입니다. `SpringDataJpaUserRepository`는 인터페이스일 뿐인데도, `save`, `findAll`, `findById`, `delete`가 실제로 동작합니다. 이는 Spring Data JPA가 인터페이스 선언을 읽고 런타임에 프록시 구현체를 만들어 주기 때문입니다. 그래서 개발자는 데이터 접근 계층의 반복 코드를 줄이고, 도메인에 필요한 조회 메서드를 인터페이스 형태로 더 간결하게 표현할 수 있습니다.

또 하나 중요했던 점은 Repository 인터페이스가 단순 CRUD 저장소를 넘어서 "의도 표현 도구" 역할도 한다는 것입니다. `findByEmail`, `existsByEmail` 같은 메서드 이름만 봐도 어떤 조회를 하려는지 바로 드러나고, 서비스 계층은 그 메서드를 조합해 중복 검증이나 사용자 검색 같은 비즈니스 흐름을 만들 수 있습니다. 학습 단계에서는 이 구조를 이해하면 JPA를 "복잡한 ORM 기술"로 보기보다, 객체 중심으로 데이터를 다루기 위한 추상화 계층으로 받아들이기 쉬워집니다.
