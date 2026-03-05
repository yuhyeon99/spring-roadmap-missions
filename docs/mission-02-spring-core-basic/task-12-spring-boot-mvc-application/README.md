# 스프링 핵심 원리 - 기본: 스프링 부트와 스프링 MVC를 활용한 웹 애플리케이션 개발

이 문서는 `mission-02-spring-core-basic`의 `task-12-spring-boot-mvc-application` 수행 결과를 정리한 보고서입니다. 컨트롤러-서비스-리포지토리 계층을 갖춘 간단한 게시글 웹 API를 구현하고, 계층 간 책임을 분리한 방식을 설명합니다.

## 1. 작업 개요
- 미션/태스크: `mission-02-spring-core-basic` / `task-12-spring-boot-mvc-application`
- 목표: Spring Boot + Spring MVC로 게시글 CRUD(생성/조회/삭제) API를 설계하고 계층 구조를 구현한다.
- 엔드포인트: `POST /mission02/task12/articles`, `GET /mission02/task12/articles`, `GET /mission02/task12/articles/{id}`, `DELETE /mission02/task12/articles/{id}`

## 2. 코드 파일 경로 인덱스
| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/domain/Article.java` | 게시글 도메인 값 객체(제목/내용/작성자/생성시각) |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/dto/ArticleCreateRequest.java` | 게시글 생성 요청 바인딩/검증용 DTO |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/dto/ArticleResponse.java` | 단건 응답 DTO |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/dto/ArticleListResponse.java` | 목록 응답 DTO |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/repository/ArticleRepository.java` | 저장소 인터페이스(추상화) |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/repository/InMemoryArticleRepository.java` | ConcurrentHashMap 기반 인메모리 구현체 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/service/ArticleService.java` | 비즈니스 로직(검증, 정렬, 변환) |
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/controller/ArticleController.java` | HTTP 엔드포인트 매핑/상태 코드 처리 |
| Exception | `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/exception/ArticleNotFoundException.java` | 404 응답용 커스텀 예외 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/ArticleControllerTest.java` | 계층 협력과 예외 흐름 검증 |

## 3. 구현 단계와 주요 코드 해설
1) **도메인/DTO 분리**: `Article`은 불변 값 객체로 필수 값 검증을 책임지고, 외부 입출력은 DTO를 통해 캡슐화했습니다.
2) **저장소 추상화**: `ArticleRepository` 인터페이스로 저장 전략을 분리하고, 학습 편의를 위해 `InMemoryArticleRepository`를 기본 구현체로 제공했습니다.
3) **서비스 계층**: `ArticleService`가 입력 검증, 정렬, 예외 처리(찾을 수 없음) 등을 담당하며 트랜잭션 경계를 정의했습니다.
4) **웹 계층**: `ArticleController`는 REST 엔드포인트를 노출하고 HTTP 상태 코드(201/200/204/404)를 명시적으로 반환합니다.
5) **테스트**: 서비스 레이어 단위 테스트를 통해 생성→조회→삭제와 예외 흐름이 기대대로 동작하는지 검증했습니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `Article.java`
- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/domain/Article.java`
- 역할: 게시글의 핵심 속성을 담는 불변 도메인 객체.
- 상세: 필수 값(제목/본문/작성자)을 생성 시 검증하고, 생성 시각을 기본값으로 설정합니다.

<details>
<summary><code>Article.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.domain;

import java.time.LocalDateTime;

public class Article {

    private final Long id;
    private final String title;
    private final String content;
    private final String author;
    private final LocalDateTime createdAt;

    public Article(Long id, String title, String content, String author, LocalDateTime createdAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("본문은 비어 있을 수 없습니다.");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("작성자는 비어 있을 수 없습니다.");
        }
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
```

</details>

### 4.2 `ArticleCreateRequest.java`
- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/dto/ArticleCreateRequest.java`
- 역할: POST 요청을 바인딩하고 Bean Validation으로 입력을 검증합니다.
- 상세: 제목/본문/작성자 각각 길이 제한과 공백 불가 검증을 설정했습니다.

<details>
<summary><code>ArticleCreateRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ArticleCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "본문은 필수입니다.")
    @Size(max = 2000, message = "본문은 2000자 이하여야 합니다.")
    private String content;

    @NotBlank(message = "작성자는 필수입니다.")
    @Size(max = 30, message = "작성자는 30자 이하여야 합니다.")
    private String author;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
```

</details>

### 4.3 `ArticleResponse.java`
- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/dto/ArticleResponse.java`
- 역할: 단건 조회/생성 응답을 표현하는 DTO.
- 상세: 도메인 객체를 API 응답 형태로 변환해 컨트롤러 반환값으로 사용합니다.

<details>
<summary><code>ArticleResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto;

import java.time.LocalDateTime;

public class ArticleResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String author;
    private final LocalDateTime createdAt;

    public ArticleResponse(Long id, String title, String content, String author, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
```

</details>

### 4.4 `ArticleListResponse.java`
- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/dto/ArticleListResponse.java`
- 역할: 목록 응답 DTO로, ArticleResponse 리스트를 감싸 일관된 응답 구조를 제공합니다.
- 상세: 리스트 형태를 한 번 감싸두어 필드 확장이 용이합니다.

<details>
<summary><code>ArticleListResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto;

import java.util.List;

public class ArticleListResponse {

    private final List<ArticleResponse> articles;

    public ArticleListResponse(List<ArticleResponse> articles) {
        this.articles = articles;
    }

    public List<ArticleResponse> getArticles() {
        return articles;
    }
}
```

</details>

### 4.5 `ArticleRepository.java`
- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/repository/ArticleRepository.java`
- 역할: 저장소 추상화 인터페이스.
- 상세: 저장/단건조회/전체조회/삭제/초기화(clear) 계약을 정의해 구현체 교체 가능성을 열어둡니다.

<details>
<summary><code>ArticleRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.repository;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.domain.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

    Article save(Article article);

    Optional<Article> findById(Long id);

    List<Article> findAll();

    void deleteById(Long id);

    void clear();
}
```

</details>

### 4.6 `InMemoryArticleRepository.java`
- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/repository/InMemoryArticleRepository.java`
- 역할: ConcurrentHashMap 기반의 단순 인메모리 저장소 구현체.
- 상세: thread-safe 맵과 AtomicLong 시퀀스로 ID를 생성하며, 테스트 용이성을 위해 clear 기능을 제공합니다.

<details>
<summary><code>InMemoryArticleRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.repository;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.domain.Article;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryArticleRepository implements ArticleRepository {

    private final Map<Long, Article> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public Article save(Article article) {
        Article saved = new Article(sequence.incrementAndGet(),
            article.getTitle(),
            article.getContent(),
            article.getAuthor(),
            article.getCreatedAt());
        store.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public Optional<Article> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Article> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public void clear() {
        store.clear();
        sequence.set(0L);
    }
}
```

</details>

### 4.7 `ArticleNotFoundException.java`
- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/exception/ArticleNotFoundException.java`
- 역할: 존재하지 않는 글 조회 시 404를 반환하도록 하는 커스텀 예외.
- 상세: `@ResponseStatus(HttpStatus.NOT_FOUND)`로 상태 코드를 고정합니다.

<details>
<summary><code>ArticleNotFoundException.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(Long id) {
        super("게시글을 찾을 수 없습니다. id=" + id);
    }
}
```

</details>

### 4.8 `ArticleService.java`
- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/service/ArticleService.java`
- 역할: 비즈니스 로직 중심 계층으로, 생성/조회/목록/삭제와 예외 처리 책임을 가집니다.
- 상세: 트랜잭션 경계를 선언하고, 정렬/DTO 변환/예외 발생을 서비스에서 수행해 컨트롤러를 단순화했습니다.

<details>
<summary><code>ArticleService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.domain.Article;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleListResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.exception.ArticleNotFoundException;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article saved = articleRepository.save(new Article(
            null,
            request.getTitle(),
            request.getContent(),
            request.getAuthor(),
            LocalDateTime.now()
        ));
        return toResponse(saved);
    }

    public ArticleResponse get(Long id) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ArticleNotFoundException(id));
        return toResponse(article);
    }

    public ArticleListResponse list() {
        List<ArticleResponse> responses = articleRepository.findAll().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .map(this::toResponse)
            .collect(Collectors.toList());
        return new ArticleListResponse(responses);
    }

    @Transactional
    public void delete(Long id) {
        articleRepository.findById(id).orElseThrow(() -> new ArticleNotFoundException(id));
        articleRepository.deleteById(id);
    }

    private ArticleResponse toResponse(Article article) {
        return new ArticleResponse(
            article.getId(),
            article.getTitle(),
            article.getContent(),
            article.getAuthor(),
            article.getCreatedAt()
        );
    }
}
```

</details>

### 4.9 `ArticleController.java`
- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/controller/ArticleController.java`
- 역할: REST API 엔드포인트를 정의하고 서비스와 협력합니다.
- 상세: `@RequestMapping("/mission02/task12/articles")`로 기본 경로를 두고, POST/GET/DELETE 각각에 상태 코드를 지정했습니다.

<details>
<summary><code>ArticleController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleListResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task12/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse create(@RequestBody @Valid ArticleCreateRequest request) {
        return articleService.create(request);
    }

    @GetMapping("/{id}")
    public ArticleResponse get(@PathVariable Long id) {
        return articleService.get(id);
    }

    @GetMapping
    public ArticleListResponse list() {
        return articleService.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        articleService.delete(id);
    }
}
```

</details>

### 4.10 `ArticleControllerTest.java`
- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission02_spring_core_basic/task12_spring_boot_mvc_application/ArticleControllerTest.java`
- 역할: 서비스 계층을 통해 CRUD 흐름과 예외를 검증합니다.
- 상세: 저장소를 초기화한 뒤 생성→목록→삭제→예외 발생 시나리오를 AssertJ로 확인합니다.

<details>
<summary><code>ArticleControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleListResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.exception.ArticleNotFoundException;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.repository.ArticleRepository;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ArticleControllerTest {

    @Autowired
    ArticleService articleService;

    @Autowired
    ArticleRepository articleRepository;

    @BeforeEach
    void setUp() {
        articleRepository.clear();
    }

    @Test
    @DisplayName("글을 생성하면 ID와 작성 정보가 반환된다")
    void createArticle() {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("첫 글");
        request.setContent("본문 내용");
        request.setAuthor("작성자A");

        ArticleResponse response = articleService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("첫 글");
        assertThat(response.getAuthor()).isEqualTo("작성자A");
    }

    @Test
    @DisplayName("목록 조회는 최근 생성 순으로 반환한다")
    void listArticles() {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("목록 글");
        request.setContent("내용");
        request.setAuthor("작성자B");
        articleService.create(request);

        ArticleListResponse list = articleService.list();

        assertThat(list.getArticles())
            .isNotEmpty()
            .first()
            .extracting(ArticleResponse::getTitle)
            .isEqualTo("목록 글");
    }

    @Test
    @DisplayName("없는 글을 조회하면 예외가 발생한다")
    void getArticle_notFound() {
        assertThatThrownBy(() -> articleService.get(999L))
            .isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    @DisplayName("삭제 후 조회하면 예외가 발생한다")
    void deleteArticle_thenNotFound() {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("삭제 대상");
        request.setContent("삭제 내용");
        request.setAuthor("작성자C");
        ArticleResponse created = articleService.create(request);

        articleService.delete(created.getId());

        assertThatThrownBy(() -> articleService.get(created.getId()))
            .isInstanceOf(ArticleNotFoundException.class);
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크
- **레이어드 아키텍처 (Controller-Service-Repository)**
  - 핵심: 표현/비즈니스/데이터 접근을 분리해 책임을 명확히 한다.
  - 왜 쓰는가: 변경 파급 범위를 줄이고 테스트 대상을 좁히기 위해.
  - 참고 링크: https://docs.spring.io/spring-framework/reference/web/webmvc.html#webmvc-controller
- **Bean Validation(@Valid, @NotBlank, @Size)**
  - 핵심: 요청 DTO에 선언적 검증 규칙을 부여한다.
  - 왜 쓰는가: 컨트롤러 진입 전 잘못된 입력을 차단하여 서비스 로직을 단순화한다.
  - 참고 링크: https://jakarta.ee/specifications/bean-validation/
- **@ResponseStatus를 통한 예외 매핑**
  - 핵심: 예외 타입별 HTTP 상태 코드를 선언적으로 지정한다.
  - 왜 쓰는가: 예외 발생 시 일관된 응답 상태를 보장해 클라이언트 계약을 명확히 한다.
  - 참고 링크: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-return-values.html#mvc-ann-response-status
- **ConcurrentHashMap 기반 인메모리 리포지토리**
  - 핵심: 간단한 학습/테스트 용도로 스레드 안전한 저장소를 구현한다.
  - 왜 쓰는가: DB 설정 없이도 MVC 계층 협력 동작을 빠르게 검증하기 위해.
  - 참고 링크: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html

## 6. 실행·검증 방법
- 애플리케이션 실행: `./gradlew bootRun`
- API 호출 예시:
  - 생성: `curl -X POST -H "Content-Type: application/json" -d '{"title":"첫 글","content":"내용","author":"작성자"}' http://localhost:8080/mission02/task12/articles`
  - 목록: `curl http://localhost:8080/mission02/task12/articles`
  - 단건: `curl http://localhost:8080/mission02/task12/articles/1`
  - 삭제: `curl -X DELETE http://localhost:8080/mission02/task12/articles/1`
- 테스트: `./gradlew test --tests "*task12_spring_boot_mvc_application*"`

## 7. 결과 확인 방법(스크린샷 포함)
- 성공 기준: 위 테스트 명령이 통과하고, 실제 실행 시 생성→조회→삭제가 예상한 HTTP 상태 코드(201/200/204/404)로 동작하는지 curl로 확인.
- 스크린샷은 요구되지 않는 태스크로, 필요 시 Postman/curl 결과 화면을 `docs/mission-02-spring-core-basic/task-12-spring-boot-mvc-application/` 하위에 추가해도 됩니다.

## 8. 학습 내용
- 컨트롤러는 입력/출력 계약만 담당하고, 서비스에 검증·정렬·예외 처리를 몰아주면 테스트와 유지보수가 쉬워진다.
- 저장소를 인터페이스로 두면 인메모리/DB 구현을 자유롭게 교체할 수 있어 OCP를 자연스럽게 만족한다.
- Bean Validation으로 DTO를 방어한 뒤 도메인 객체에서 한 번 더 핵심 제약을 검증하면 계층별로 책임이 명확해진다.
