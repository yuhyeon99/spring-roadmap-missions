# HTTP 메서드별 특징 분석 및 실습

`mission-03-http-web-basic`의 `task-01-http-methods` 보고서입니다. 주요 HTTP 메서드의 특징을 정리하고, 간단한 노트 리소스로 GET/POST/PUT/DELETE 사용 사례를 직접 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-03-http-web-basic` / `task-01-http-methods`
- 목표:
  - HTTP 메서드(GET, POST, PUT, DELETE)의 특징·안전성·멱등성·적합한 상황을 표로 정리한다.
  - 각 메서드가 실제로 어떻게 동작하는지 확인할 수 있는 REST 엔드포인트를 구현한다.

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission03_http_web_basic/task01_http_methods/controller/HttpMethodsDemoController.java` | HTTP 메서드별 데모 엔드포인트 제공 `/mission03/task01/notes` |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission03_http_web_basic/task01_http_methods/service/HttpMethodNoteService.java` | 노트 리소스 생성/조회/대체/삭제 비즈니스 로직 |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission03_http_web_basic/task01_http_methods/repository/HttpMethodNoteRepository.java` | ConcurrentMap 기반 인메모리 저장소, ID 시퀀스 관리 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission03_http_web_basic/task01_http_methods/domain/HttpMethodNote.java` | 노트 리소스 표현(record) |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission03_http_web_basic/task01_http_methods/dto/HttpMethodNoteRequest.java` | 요청 본문(title, content) 검증용 DTO |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission03_http_web_basic/task01_http_methods/HttpMethodsDemoControllerTest.java` | MockMvc로 메서드별 동작과 상태 코드 검증 |

## 3. 구현 단계와 주요 코드 해설

1) **HTTP 메서드 핵심 비교 표**

| 메서드 | 목적 | 안전성 | 멱등성 | 본문 허용 | 대표 사용 사례 |
|---|---|---|---|---|---|
| GET | 리소스 조회 | 안전 ✔ | 멱등 ✔ | 본문 비권장 | 리스트/단건 조회, 캐시 가능 응답 |
| POST | 리소스 생성, 처리 요청 | 안전 ✖ | 멱등 ✖ | 허용 | 회원 가입, 주문 생성, 서버 측 액션 트리거 |
| PUT | 지정 리소스 전체 대체(또는 생성) | 안전 ✖ | 멱등 ✔ | 허용 | 특정 ID 문서 전체 업데이트, 없으면 생성(업서트) |
| DELETE | 리소스 제거 | 안전 ✖ | 멱등 ✔ | 일반적으로 없음 | 단건 삭제, 소프트 삭제 트리거 |

2) **노트 리소스 설계**
- 경로: `/mission03/task01/notes`
- 모델: `id`, `title`, `content`, `updatedAt`
- 저장: `ConcurrentHashMap` + `AtomicLong` 시퀀스 (다중 요청에서도 안전하게 증가)

3) **메서드별 흐름 요약**
- `GET /notes` → 전체 목록 조회, 정렬된 리스트 반환.
- `GET /notes/{id}` → 없으면 404.
- `POST /notes` → 본문 검증 후 생성, `201 Created` + `Location` 헤더로 새 리소스 경로 제공.
- `PUT /notes/{id}` → 동일 ID를 항상 같은 상태로 맞추는 업서트(upsert), 멱등성 데모.
- `DELETE /notes/{id}` → 자원이 없어도 추가 부작용 없이 제거, `204 No Content`.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `HttpMethodsDemoController.java`
- 역할: HTTP 메서드 매핑 및 응답 구성.
- 매핑: `@RequestMapping("/mission03/task01/notes")`
  - `GET /` 목록, `GET /{id}` 단건, `POST /` 생성(201 + Location), `PUT /{id}` 전체 대체, `DELETE /{id}` 삭제.
- 특징: `ServletUriComponentsBuilder`로 Location 헤더 생성.

<details>
<summary><code>HttpMethodsDemoController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.controller;

import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.domain.HttpMethodNote;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.dto.HttpMethodNoteRequest;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.service.HttpMethodNoteService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/mission03/task01/notes")
public class HttpMethodsDemoController {

    private final HttpMethodNoteService httpMethodNoteService;

    public HttpMethodsDemoController(HttpMethodNoteService httpMethodNoteService) {
        this.httpMethodNoteService = httpMethodNoteService;
    }

    @GetMapping
    public List<HttpMethodNote> list() {
        return httpMethodNoteService.listNotes();
    }

    @GetMapping("/{id}")
    public HttpMethodNote find(@PathVariable Long id) {
        return httpMethodNoteService.find(id);
    }

    @PostMapping
    public ResponseEntity<HttpMethodNote> create(@RequestBody HttpMethodNoteRequest request) {
        HttpMethodNote created = httpMethodNoteService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HttpMethodNote> replace(@PathVariable Long id, @RequestBody HttpMethodNoteRequest request) {
        HttpMethodNote replaced = httpMethodNoteService.replace(id, request);
        return ResponseEntity.ok(replaced);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        httpMethodNoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

</details>

### 4.2 `HttpMethodNoteService.java`
- 역할: 요청 검증, 예외 처리, 저장소 호출을 묶는 서비스 계층.
- 검증: 빈 제목/내용 또는 잘못된 ID(<1) 시 `400 Bad Request`.
- 멱등성: `replace`가 업서트이므로 동일 요청 반복해도 리소스 상태가 같게 유지.

<details>
<summary><code>HttpMethodNoteService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.service;

import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.domain.HttpMethodNote;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.dto.HttpMethodNoteRequest;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.repository.HttpMethodNoteRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HttpMethodNoteService {

    private final HttpMethodNoteRepository repository;

    public HttpMethodNoteService(HttpMethodNoteRepository repository) {
        this.repository = repository;
    }

    public List<HttpMethodNote> listNotes() {
        return repository.findAll();
    }

    public HttpMethodNote find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다."));
    }

    public HttpMethodNote create(HttpMethodNoteRequest request) {
        request.validate();
        return repository.create(request.title().trim(), request.content().trim());
    }

    public HttpMethodNote replace(Long id, HttpMethodNoteRequest request) {
        request.validate();
        if (id < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id는 1 이상의 값이어야 합니다.");
        }
        return repository.upsert(id, request.title().trim(), request.content().trim());
    }

    public void delete(Long id) {
        repository.delete(id);
    }
}
```

</details>

### 4.3 `HttpMethodNoteRepository.java`
- 역할: 동시성 안전한 인메모리 저장소, ID 시퀀스 관리.
- 주요 메서드: `create`(자동 ID 발급), `upsert`(PUT 멱등 구현), `findAll` 정렬, `delete`.

<details>
<summary><code>HttpMethodNoteRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.repository;

import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.domain.HttpMethodNote;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class HttpMethodNoteRepository {

    private final ConcurrentMap<Long, HttpMethodNote> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public List<HttpMethodNote> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparingLong(HttpMethodNote::id))
                .toList();
    }

    public Optional<HttpMethodNote> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public HttpMethodNote create(String title, String content) {
        long id = sequence.incrementAndGet();
        HttpMethodNote note = new HttpMethodNote(id, title, content, LocalDateTime.now());
        store.put(id, note);
        return note;
    }

    public HttpMethodNote upsert(Long id, String title, String content) {
        sequence.updateAndGet(current -> Math.max(current, id));
        HttpMethodNote note = new HttpMethodNote(id, title, content, LocalDateTime.now());
        store.put(id, note);
        return note;
    }

    public void delete(Long id) {
        store.remove(id);
    }

    public void clear() {
        store.clear();
        sequence.set(0);
    }
}
```

</details>

### 4.4 `HttpMethodNote.java`
- 역할: 노트 리소스를 나타내는 불변 record.
- 특징: `withUpdatedContent`로 동일 ID의 내용만 바꿔 새 인스턴스 생성.

<details>
<summary><code>HttpMethodNote.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.domain;

import java.time.LocalDateTime;

/**
 * HTTP 메서드 실습용 단순 노트 리소스.
 */
public record HttpMethodNote(
        Long id,
        String title,
        String content,
        LocalDateTime updatedAt
) {
    public HttpMethodNote withUpdatedContent(String newTitle, String newContent, LocalDateTime updatedAt) {
        return new HttpMethodNote(this.id, newTitle, newContent, updatedAt);
    }
}
```

</details>

### 4.5 `HttpMethodNoteRequest.java`
- 역할: POST/PUT 요청 본문 DTO, 비어 있는 값에 대해 400 예외 발생.

<details>
<summary><code>HttpMethodNoteRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.dto;

import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public record HttpMethodNoteRequest(String title, String content) {

    public void validate() {
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title과 content는 필수입니다.");
        }
    }
}
```

</details>

### 4.6 `HttpMethodsDemoControllerTest.java`
- 역할: MockMvc standalone 설정으로 메서드별 동작과 상태 코드를 검증.
- 시나리오: POST 생성(201 + Location), GET 목록 확인, PUT 멱등성(동일 ID 반복 호출 후 크기 1 유지), DELETE 후 빈 리스트.

<details>
<summary><code>HttpMethodsDemoControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods;

import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.controller.HttpMethodsDemoController;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.dto.HttpMethodNoteRequest;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.repository.HttpMethodNoteRepository;
import com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.service.HttpMethodNoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HttpMethodsDemoControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private HttpMethodNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new HttpMethodNoteRepository();
        HttpMethodNoteService service = new HttpMethodNoteService(repository);
        mockMvc = MockMvcBuilders.standaloneSetup(new HttpMethodsDemoController(service)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void postCreatesNoteAndReturns201() throws Exception {
        HttpMethodNoteRequest request = new HttpMethodNoteRequest("GET 소개", "GET은 리소스를 조회합니다.");

        mockMvc.perform(post("/mission03/task01/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/mission03/task01/notes/")))
                .andExpect(jsonPath("$.title").value("GET 소개"))
                .andExpect(jsonPath("$.content").value("GET은 리소스를 조회합니다."));
    }

    @Test
    void getReturnsListAfterPost() throws Exception {
        HttpMethodNoteRequest request = new HttpMethodNoteRequest("POST 소개", "POST는 리소스를 생성합니다.");

        mockMvc.perform(post("/mission03/task01/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/mission03/task01/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("POST 소개"));
    }

    @Test
    void putIsIdempotentForSamePayload() throws Exception {
        HttpMethodNoteRequest request = new HttpMethodNoteRequest("PUT 소개", "PUT은 전체 리소스를 대체합니다.");

        mockMvc.perform(put("/mission03/task01/notes/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("PUT 소개"));

        mockMvc.perform(put("/mission03/task01/notes/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/mission03/task01/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void deleteRemovesResource() throws Exception {
        HttpMethodNoteRequest request = new HttpMethodNoteRequest("DELETE 소개", "DELETE는 리소스를 제거합니다.");

        mockMvc.perform(post("/mission03/task01/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/mission03/task01/notes/{id}", 1))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/mission03/task01/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

- **GET: 안전하며 리소스 상태를 바꾸지 않는 조회 메서드**
  - 핵심: 서버 자원을 변경하지 않으므로 동일 요청 반복 시 결과가 같다(안전 + 멱등).
  - 왜 쓰는가: 캐시·프리패칭에 적합, 링크 공유 시 부작용 없음.
  - 참고 링크: https://www.rfc-editor.org/rfc/rfc9110.html#name-get

- **POST: 비멱등 요청 본문 처리/리소스 생성**
  - 핵심: 서버가 요청 본문을 처리하여 새 리소스를 만들거나 서버 측 동작을 실행한다.
  - 왜 쓰는가: 생성, 명령 실행 등 상태 변화를 의도할 때 사용.
  - 참고 링크: https://www.rfc-editor.org/rfc/rfc9110.html#name-post

- **PUT: 지정 URI의 전체 교체(멱등)**
  - 핵심: 같은 본문으로 반복 호출해도 자원 최종 상태는 동일해야 한다.
  - 왜 쓰는가: 명확한 ID가 있을 때 전체 업데이트/업서트에 적합.
  - 참고 링크: https://www.rfc-editor.org/rfc/rfc9110.html#name-put

- **DELETE: 자원 제거(멱등)**
  - 핵심: 여러 번 호출해도 최종적으로 자원이 없는 상태로 수렴해야 한다.
  - 왜 쓰는가: 서버 자원 해제, 소프트 삭제 트리거 등.
  - 참고 링크: https://www.rfc-editor.org/rfc/rfc9110.html#name-delete

- **201 Created + Location 헤더**
  - 핵심: 새로 생성된 리소스의 고유 URI를 응답 헤더로 알려 클라이언트가 후속 조회/갱신에 활용하도록 한다.
  - 왜 쓰는가: 생성 후 즉시 리소스 위치를 공유해 추가 GET/PUT 요청 흐름을 단순화한다.
  - 참고 링크: https://www.rfc-editor.org/rfc/rfc9110.html#name-201-created

## 6. 실행·검증 방법

1) 애플리케이션 실행
```bash
./gradlew bootRun
```

2) POST로 노트 생성 (201 + Location)
```bash
curl -i -X POST \
  -H "Content-Type: application/json" \
  -d '{"title":"HTTP GET","content":"GET은 조회"}' \
  http://localhost:8080/mission03/task01/notes
```

3) GET 목록 조회
```bash
curl http://localhost:8080/mission03/task01/notes
```

4) PUT으로 동일 ID 전체 대체(멱등 확인)
```bash
curl -X PUT -H "Content-Type: application/json" \
  -d '{"title":"PUT","content":"전체 대체"}' \
  http://localhost:8080/mission03/task01/notes/1
```

5) DELETE로 제거 후 빈 배열 확인
```bash
curl -X DELETE http://localhost:8080/mission03/task01/notes/1
curl http://localhost:8080/mission03/task01/notes
```

6) 테스트 실행
```bash
./gradlew test --tests "*HttpMethodsDemoControllerTest"
```

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
  - POST 응답이 `201 Created`이고 `Location` 헤더가 `/mission03/task01/notes/{id}` 형식으로 반환된다.
  - PUT 동일 페이로드 반복 호출 후 목록 크기가 1(멱등)이고 내용이 요청 값과 동일하다.
  - DELETE 후 목록이 빈 배열이다.
- 스크린샷: 터미널 `curl` 결과를 캡처할 경우 `docs/mission-03-http-web-basic/task-01-http-methods/curl-demo.png`에 저장해 관리한다(현재는 텍스트 검증으로 충분하여 이미지 파일은 비워둠).

## 8. 학습 내용

- 안전성(safe)과 멱등성(idempotent)은 HTTP 메서드 선택의 핵심 판단 기준이며, 실제 코드 흐름(업서트/삭제)으로 체감할 수 있다.
- `Location` 헤더를 함께 반환하면 클라이언트가 후속 GET/PUT/DELETE 경로를 헷갈리지 않고 따라갈 수 있다.
- 간단한 인메모리 저장소라도 멀티 스레드 환경을 가정해 `ConcurrentHashMap`과 `AtomicLong`을 사용하면 데모 중 발생할 수 있는 경쟁 상태를 줄일 수 있다.
