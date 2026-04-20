# 스프링 MVC: 애노테이션을 통한 요청 매핑

이 문서는 `mission-04-spring-mvc`의 `task-05-request-mapping-annotations` 수행 결과를 정리한 보고서입니다. `@RequestMapping`을 사용해 같은 URL `/mission04/task05/products`를 `GET`과 `POST` 요청으로 각각 다르게 매핑하는 예제를 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-04-spring-mvc` / `task-05-request-mapping-annotations`
- 목표:
  - `@RequestMapping` 애노테이션으로 하나의 URL에 대해 HTTP 메서드별로 다른 컨트롤러 메서드를 연결한다.
  - `GET /products`는 제품 목록을 조회하고, `POST /products`는 새 제품을 추가하는 간단한 REST 예제를 만든다.
  - 요청 본문 바인딩, 응답 상태 코드, `Location` 헤더, JSON 응답 구조를 함께 확인할 수 있도록 테스트와 문서를 정리한다.
- 엔드포인트:
  - `GET /mission04/task05/products`
  - `POST /mission04/task05/products`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/controller/ProductController.java` | 같은 URL에 대한 GET/POST 요청 매핑과 HTTP 응답 구성 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/service/ProductCatalogService.java` | 제품 목록 조회와 신규 제품 등록 로직 처리 |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/repository/ProductRepository.java` | 제품 저장소 추상화 인터페이스 |
| Repository | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/repository/InMemoryProductRepository.java` | 인메모리 제품 저장소 구현과 초기 데이터 준비 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/domain/Product.java` | 제품 엔티티 성격의 도메인 객체 |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/dto/ProductCreateRequest.java` | POST 요청 본문 바인딩용 DTO |
| DTO | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/dto/ProductResponse.java` | GET/POST 응답 JSON 구조 DTO |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/ProductControllerTest.java` | GET 목록 조회, POST 생성, 같은 URL의 메서드별 매핑 검증 |

## 3. 구현 단계와 주요 코드 해설

1. `ProductController`에 클래스 레벨 `@RequestMapping("/mission04/task05/products")`를 두고, 메서드 레벨에서는 `@RequestMapping(method = RequestMethod.GET)`와 `@RequestMapping(method = RequestMethod.POST)`를 사용해 같은 URL을 HTTP 메서드별로 나눠 매핑했습니다.
2. `GET` 메서드는 `ProductCatalogService#listProducts()`를 호출해 현재 제품 목록을 JSON 배열로 반환합니다. 초기 데이터 두 건을 미리 준비해 두어서 요청 직후에도 목록 결과를 확인할 수 있습니다.
3. `POST` 메서드는 `@RequestBody ProductCreateRequest`로 요청 JSON을 바인딩하고, 저장 후 `201 Created`와 `Location` 헤더를 함께 반환합니다.
4. `InMemoryProductRepository`는 `ConcurrentHashMap`과 `AtomicLong`을 사용해 간단한 학습용 저장소를 구성했습니다. 별도 DB 없이도 생성과 조회 흐름을 바로 실습할 수 있습니다.
5. `ProductControllerTest`는 MockMvc로 `GET`과 `POST`를 같은 URL에 각각 호출해 상태 코드, 응답 JSON, `Location` 헤더, 생성 후 목록 증가 여부를 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `ProductController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/controller/ProductController.java`
- 역할: 같은 URL에 대한 GET/POST 요청 매핑과 HTTP 응답 구성
- 상세 설명:
- 기본 경로: `/mission04/task05/products`
- HTTP 메서드/세부 경로: `GET /mission04/task05/products`, `POST /mission04/task05/products`
- `list()`는 상태 코드 `200 OK`와 함께 제품 목록 JSON 배열을 반환합니다.
- `create()`는 요청 본문을 받아 새 제품을 저장한 뒤 `201 Created`, `Location` 헤더, 생성된 제품 JSON을 함께 반환합니다.

<details>
<summary><code>ProductController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto.ProductCreateRequest;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto.ProductResponse;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.service.ProductCatalogService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/mission04/task05/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ProductResponse> list() {
        return productCatalogService.listProducts();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest request) {
        ProductResponse created = productCatalogService.createProduct(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
```

</details>

### 4.2 `ProductCatalogService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/service/ProductCatalogService.java`
- 역할: 제품 목록 조회와 신규 제품 등록 로직 처리
- 상세 설명:
- 핵심 공개 메서드: `listProducts()`, `createProduct(ProductCreateRequest request)`
- 트랜잭션은 사용하지 않으며, 학습용 인메모리 저장소와만 협력합니다.
- 조회와 생성 모두 `ProductResponse`로 변환하면서, 어떤 HTTP 메서드로 처리된 결과인지 설명 메시지도 함께 넣었습니다.

<details>
<summary><code>ProductCatalogService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.domain.Product;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto.ProductCreateRequest;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto.ProductResponse;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductCatalogService {

    private final ProductRepository productRepository;

    public ProductCatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> listProducts() {
        return productRepository.findAll().stream()
                .map(product -> toResponse(product, "GET /products 요청으로 조회된 상품입니다."))
                .toList();
    }

    public ProductResponse createProduct(ProductCreateRequest request) {
        Product saved = productRepository.save(new Product(
                null,
                request.getName(),
                request.getPrice(),
                request.getCategory()
        ));
        return toResponse(saved, "POST /products 요청으로 새 상품이 등록되었습니다.");
    }

    private ProductResponse toResponse(Product product, String message) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                message
        );
    }
}
```

</details>

### 4.3 `ProductRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/repository/ProductRepository.java`
- 역할: 제품 저장소 추상화 인터페이스
- 상세 설명:
- 제품 목록 조회 `findAll()`과 저장 `save(Product product)` 두 동작만 노출합니다.
- 이번 태스크에서는 저장소 구현을 단순화했지만, 서비스는 인터페이스에만 의존하도록 분리했습니다.
- 덕분에 나중에 DB 저장소로 바꾸더라도 서비스와 컨트롤러 구조를 유지할 수 있습니다.

<details>
<summary><code>ProductRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.repository;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.domain.Product;
import java.util.List;

public interface ProductRepository {

    List<Product> findAll();

    Product save(Product product);
}
```

</details>

### 4.4 `InMemoryProductRepository.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/repository/InMemoryProductRepository.java`
- 역할: 인메모리 제품 저장소 구현과 초기 데이터 준비
- 상세 설명:
- `ConcurrentHashMap`에 제품을 저장하고, `AtomicLong`으로 ID를 증가시킵니다.
- 생성자에서 초기 제품 두 건을 미리 저장해 `GET /products` 호출 시 바로 목록 결과를 확인할 수 있게 했습니다.
- `findAll()`은 ID 기준으로 정렬해 응답 순서를 안정적으로 유지합니다.

<details>
<summary><code>InMemoryProductRepository.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.repository;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.domain.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final ConcurrentHashMap<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0L);

    public InMemoryProductRepository() {
        save(new Product(null, "스프링 입문서", 28000, "도서"));
        save(new Product(null, "MVC 실습 키트", 45000, "학습도구"));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values()).stream()
                .sorted((left, right) -> left.getId().compareTo(right.getId()))
                .toList();
    }

    @Override
    public Product save(Product product) {
        long id = sequence.incrementAndGet();
        Product saved = new Product(id, product.getName(), product.getPrice(), product.getCategory());
        store.put(id, saved);
        return saved;
    }
}
```

</details>

### 4.5 `Product.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/domain/Product.java`
- 역할: 제품 엔티티 성격의 도메인 객체
- 상세 설명:
- 제품 ID, 이름, 가격, 카테고리를 보관하는 단순 읽기 전용 객체입니다.
- 저장소 내부에서는 이 객체를 기준으로 목록과 신규 제품을 관리합니다.
- 학습 목적상 최소 필드만 두어 요청 매핑과 응답 구조에 집중할 수 있게 했습니다.

<details>
<summary><code>Product.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.domain;

public class Product {

    private final Long id;
    private final String name;
    private final int price;
    private final String category;

    public Product(Long id, String name, int price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }
}
```

</details>

### 4.6 `ProductCreateRequest.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/dto/ProductCreateRequest.java`
- 역할: POST 요청 본문 바인딩용 DTO
- 상세 설명:
- `@RequestBody`가 JSON 요청 본문을 이 객체로 바인딩합니다.
- `name`, `price`, `category` 필드를 받아 신규 제품 생성에 필요한 최소 입력값만 다룹니다.
- 이번 태스크는 요청 매핑 학습이 중심이므로 검증 애노테이션은 붙이지 않고 바인딩 구조에 집중했습니다.

<details>
<summary><code>ProductCreateRequest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto;

public class ProductCreateRequest {

    private String name;
    private int price;
    private String category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
```

</details>

### 4.7 `ProductResponse.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/dto/ProductResponse.java`
- 역할: GET/POST 응답 JSON 구조 DTO
- 상세 설명:
- 제품 정보와 함께 어떤 요청 처리 결과인지 설명하는 `message` 필드를 포함합니다.
- 목록 조회와 생성 응답이 같은 JSON 구조를 쓰도록 맞춰 응답 형태를 단순하게 유지했습니다.
- 테스트와 문서의 응답 예시도 이 구조를 기준으로 작성했습니다.

<details>
<summary><code>ProductResponse.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto;

public class ProductResponse {

    private final Long id;
    private final String name;
    private final int price;
    private final String category;
    private final String message;

    public ProductResponse(Long id, String name, int price, String category, String message) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }
}
```

</details>

### 4.8 `ProductControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task05_request_mapping_annotations/ProductControllerTest.java`
- 역할: GET 목록 조회, POST 생성, 같은 URL의 메서드별 매핑 검증
- 상세 설명:
- 검증 시나리오 1: `GET /products`가 초기 제품 두 건을 반환하는지 확인합니다.
- 검증 시나리오 2: `POST /products`가 새 제품을 생성하고 `201 Created`와 `Location` 헤더를 반환하는지 확인합니다.
- 검증 시나리오 3: 같은 URL이라도 `GET`과 `POST`가 다른 메서드로 매핑되고, POST 이후 GET 결과에 새 제품이 포함되는지 확인합니다.

<details>
<summary><code>ProductControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.controller.ProductController;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.repository.InMemoryProductRepository;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.service.ProductCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InMemoryProductRepository repository = new InMemoryProductRepository();
        ProductCatalogService service = new ProductCatalogService(repository);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(service)).build();
    }

    @Test
    @DisplayName("GET /products 는 현재 상품 목록을 반환한다")
    void getProductsReturnsList() throws Exception {
        mockMvc.perform(get("/mission04/task05/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("스프링 입문서"))
                .andExpect(jsonPath("$[0].message").value("GET /products 요청으로 조회된 상품입니다."));
    }

    @Test
    @DisplayName("POST /products 는 새 상품을 추가하고 201 Created를 반환한다")
    void postProductsCreatesNewProduct() throws Exception {
        mockMvc.perform(post("/mission04/task05/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "요청 매핑 실습 노트",
                                  "price": 19000,
                                  "category": "문구"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/mission04/task05/products/3")))
                .andExpect(jsonPath("$.name").value("요청 매핑 실습 노트"))
                .andExpect(jsonPath("$.price").value(19000))
                .andExpect(jsonPath("$.category").value("문구"))
                .andExpect(jsonPath("$.message").value("POST /products 요청으로 새 상품이 등록되었습니다."));
    }

    @Test
    @DisplayName("같은 URL 이라도 GET 과 POST 는 서로 다른 메서드로 매핑된다")
    void getAndPostAreMappedSeparatelyOnSameUrl() throws Exception {
        mockMvc.perform(post("/mission04/task05/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "REST API 연습 세트",
                                  "price": 32000,
                                  "category": "학습도구"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/mission04/task05/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].name").value("REST API 연습 세트"));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 `@RequestMapping`

- 핵심: URL 경로, HTTP 메서드, 헤더 조건 등을 기준으로 요청을 컨트롤러 클래스나 메서드에 매핑하는 스프링 MVC 애노테이션입니다.
- 왜 쓰는가: 하나의 컨트롤러 아래에서 공통 경로를 묶고, 메서드별로 세부 요청 조건을 나눠 표현할 수 있어 라우팅 구조가 명확해집니다.
- 참고 링크:
  - Spring Framework Annotated Controllers: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html
  - Spring Framework `@RequestMapping` Javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMapping.html

### 5.2 `RequestMethod`

- 핵심: 같은 URL이라도 `GET`, `POST`, `PUT`, `DELETE` 같은 HTTP 메서드에 따라 다른 메서드로 분기할 수 있게 해 주는 열거형입니다.
- 왜 쓰는가: `/products`라는 같은 경로를 조회와 생성처럼 서로 다른 의미로 구분할 수 있어 REST 스타일 설계와 잘 맞습니다.
- 참고 링크:
  - Spring Framework `RequestMethod` Javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMethod.html
  - RFC 9110 HTTP Semantics: https://datatracker.ietf.org/doc/html/rfc9110

### 5.3 `@RequestBody`와 `ResponseEntity`

- 핵심: `@RequestBody`는 JSON 요청 본문을 자바 객체로 바인딩하고, `ResponseEntity`는 상태 코드와 헤더를 포함한 HTTP 응답을 명시적으로 구성합니다.
- 왜 쓰는가: POST 요청에서 본문 데이터를 받아 새 자원을 만들고, `201 Created`와 `Location` 헤더를 함께 내려 주는 API 응답을 깔끔하게 표현할 수 있습니다.
- 참고 링크:
  - Spring Framework Message Converters / `@RequestBody`: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html
  - Spring Framework `ResponseEntity` Javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ResponseEntity.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 제품 목록 조회

```bash
curl -i http://localhost:8080/mission04/task05/products
```

- 예상 결과:
  - 상태 코드 `200 OK`
  - JSON 배열 2건 반환

### 6.3 새 제품 등록

```bash
curl -i -X POST http://localhost:8080/mission04/task05/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "요청 매핑 실습 노트",
    "price": 19000,
    "category": "문구"
  }'
```

- 예상 결과:
  - 상태 코드 `201 Created`
  - `Location: /mission04/task05/products/3` 헤더 포함
  - 응답 본문에 생성된 제품 정보 포함

### 6.4 등록 후 다시 목록 조회

```bash
curl -i http://localhost:8080/mission04/task05/products
```

- 예상 결과:
  - 상태 코드 `200 OK`
  - JSON 배열 3건 반환
  - 마지막 항목에 방금 추가한 제품 포함

### 6.5 테스트 실행

```bash
./gradlew test --tests com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.ProductControllerTest
```

- 예상 결과:
  - `BUILD SUCCESSFUL` 출력

## 7. 결과 확인 방법

- 성공 기준:
  - `GET /mission04/task05/products`가 현재 제품 목록을 `200 OK`로 반환합니다.
  - `POST /mission04/task05/products`가 새 제품을 생성하고 `201 Created`와 `Location` 헤더를 반환합니다.
  - 같은 URL이라도 GET과 POST 요청이 각각 다른 메서드로 처리됩니다.
- API 확인 방법:
  - 터미널에서 `curl`로 GET과 POST를 각각 호출합니다.
  - 응답 상태 코드, `Location` 헤더, JSON 본문을 확인합니다.
- 응답 결과 예시:

```http
HTTP/1.1 200
Content-Type: application/json

[
  {
    "id": 1,
    "name": "스프링 입문서",
    "price": 28000,
    "category": "도서",
    "message": "GET /products 요청으로 조회된 상품입니다."
  },
  {
    "id": 2,
    "name": "MVC 실습 키트",
    "price": 45000,
    "category": "학습도구",
    "message": "GET /products 요청으로 조회된 상품입니다."
  }
]
```

```http
HTTP/1.1 201
Location: http://localhost:8080/mission04/task05/products/3
Content-Type: application/json

{
  "id": 3,
  "name": "요청 매핑 실습 노트",
  "price": 19000,
  "category": "문구",
  "message": "POST /products 요청으로 새 상품이 등록되었습니다."
}
```

- 스크린샷 파일명과 저장 위치:
  - 현재 저장소에는 스크린샷 파일을 추가하지 않았습니다.
  - API 호출 결과 캡처가 필요하면 `task05-products-get.png`, `task05-products-post.png` 파일명으로 저장하고 경로는 `docs/mission-04-spring-mvc/task-05-request-mapping-annotations/`를 사용하면 됩니다.

## 8. 학습 내용

- `@RequestMapping`의 핵심은 경로만 매핑하는 것이 아니라, 같은 경로라도 HTTP 메서드에 따라 서로 다른 동작을 자연스럽게 나눌 수 있다는 점입니다. 이번 예제에서는 `/mission04/task05/products` 하나로 조회와 생성을 모두 표현했습니다.
- 클래스 레벨 `@RequestMapping`은 공통 경로를 묶고, 메서드 레벨 `@RequestMapping(method = ...)`은 세부 동작을 나누는 역할을 합니다. 이 구조를 쓰면 컨트롤러가 URL 구조를 기준으로 정리되어 읽기 쉬워집니다.
- POST 요청에서는 `@RequestBody`가 JSON을 DTO로 바인딩해 주기 때문에 컨트롤러가 문자열을 직접 파싱할 필요가 없습니다. 스프링 MVC가 HTTP 메시지와 자바 객체 사이의 변환을 자동으로 맡아 주는 흐름을 이해하는 데 도움이 됩니다.
- `ResponseEntity.created(...)`를 사용하면 단순히 데이터를 반환하는 것에서 끝나지 않고, HTTP 상태 코드와 헤더까지 명확하게 표현할 수 있습니다. 즉, 스프링 MVC의 요청 매핑은 라우팅뿐 아니라 올바른 HTTP 응답 구성까지 함께 생각해야 한다는 점을 보여 줍니다.
