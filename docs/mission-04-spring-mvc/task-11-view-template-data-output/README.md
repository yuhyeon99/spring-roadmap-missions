# 스프링 MVC: 뷰 템플릿을 사용한 데이터 출력

이 문서는 `mission-04-spring-mvc`의 `task-11-view-template-data-output` 수행 결과를 정리한 보고서입니다. 컨트롤러가 `Model`에 담은 제품 목록 데이터를 Thymeleaf 템플릿에서 반복문으로 출력하고, 요청 파라미터에 따라 화면에 보이는 카드 목록이 달라지는 예제를 구현했습니다.

## 1. 작업 개요

- 미션/태스크: `mission-04-spring-mvc` / `task-11-view-template-data-output`
- 목표:
  - 컨트롤러가 준비한 컬렉션 데이터를 Thymeleaf 템플릿에서 `th:each`로 반복 출력한다.
  - 요청 파라미터에 따라 같은 템플릿이 다른 데이터 조합으로 렌더링되는 흐름을 확인한다.
  - 컨트롤러, 서비스, 도메인 객체, 템플릿, 테스트를 함께 정리해 재현 가능한 결과물로 남긴다.
- 엔드포인트: `GET /mission04/task11/products`

## 2. 코드 파일 경로 인덱스

| 구분 | 파일 경로 | 역할 |
|---|---|---|
| Controller | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task11_view_template_data_output/controller/ProductCatalogPageController.java` | 요청 파라미터를 해석하고 모델 데이터를 담아 뷰를 반환하는 컨트롤러 |
| Service | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task11_view_template_data_output/service/ProductCatalogPageService.java` | 제품 목록, 카테고리 옵션, 화면 요약 수치 생성을 담당하는 서비스 |
| Domain | `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task11_view_template_data_output/domain/ProductShowcaseItem.java` | 템플릿에서 출력할 제품 한 건을 표현하는 도메인 객체 |
| Template | `src/main/resources/templates/mission04/task11/product-catalog.html` | 제품 카드 목록을 동적으로 출력하는 Thymeleaf 템플릿 |
| Test | `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task11_view_template_data_output/ProductCatalogPageControllerTest.java` | 기본 목록과 카테고리 필터 렌더링 결과를 검증하는 테스트 |

## 3. 구현 단계와 주요 코드 해설

1. `ProductCatalogPageService`에 학습용 제품 목록 5개를 메모리 데이터로 준비했습니다. 각 제품은 이름, 분류, 가격, 재고 수량, 추천 여부, 설명을 가집니다.
2. `ProductCatalogPageController`는 `GET /mission04/task11/products` 요청을 받아 `category` 파라미터를 정규화한 뒤, 필터링된 제품 목록과 요약 수치를 `Model`에 담아 논리 뷰 이름 `mission04/task11/product-catalog`를 반환합니다.
3. 템플릿 `product-catalog.html`은 `th:each="product : ${products}"`를 사용해 제품 카드 목록을 반복 출력합니다. 카드 안에서는 `th:text`, `th:if`, `th:classappend`를 사용해 추천 여부와 재고 상태를 동적으로 렌더링합니다.
4. 화면 상단 요약 카드에는 현재 화면 제품 수, 추천 상품 수, 재고 확인 필요 수, 렌더링 시각을 출력해 `Model`의 단일 값과 컬렉션 값을 함께 보여줍니다.
5. `ProductCatalogPageControllerTest`는 기본 요청과 `category=cloud` 요청을 각각 호출해 뷰 이름, 모델 속성, 실제 HTML에 렌더링된 제품 이름까지 검증합니다.

## 4. 파일별 상세 설명 + 전체 코드

### 4.1 `ProductCatalogPageController.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task11_view_template_data_output/controller/ProductCatalogPageController.java`
- 역할: 요청 파라미터를 해석하고 모델 데이터를 담아 뷰를 반환하는 컨트롤러
- 상세 설명:
- 기본 경로: `/mission04/task11/products`
- HTTP 메서드/세부 경로: `GET /mission04/task11/products`
- `category` 파라미터를 받아 필터링 분류를 결정하고, 선택된 분류명, 제품 목록, 화면 요약 수치, 렌더링 시각을 모델에 담습니다.
- 상태 코드는 기본 `200 OK`이며, 응답 형태는 Thymeleaf가 렌더링한 HTML 페이지입니다.

<details>
<summary><code>ProductCatalogPageController.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.domain.ProductShowcaseItem;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.service.ProductCatalogPageService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mission04/task11/products")
public class ProductCatalogPageController {

    private static final String VIEW_NAME = "mission04/task11/product-catalog";

    private final ProductCatalogPageService productCatalogPageService;

    public ProductCatalogPageController(ProductCatalogPageService productCatalogPageService) {
        this.productCatalogPageService = productCatalogPageService;
    }

    @GetMapping
    public String showProductCatalog(
            @RequestParam(defaultValue = "all") String category,
            Model model
    ) {
        String selectedCategory = productCatalogPageService.resolveCategory(category);
        Map<String, String> categoryOptions = productCatalogPageService.categoryOptions();
        List<ProductShowcaseItem> products = productCatalogPageService.findProducts(selectedCategory);

        model.addAttribute("pageTitle", "뷰 템플릿으로 출력하는 제품 목록");
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("selectedCategoryLabel", categoryOptions.get(selectedCategory));
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products.size());
        model.addAttribute("featuredCount", productCatalogPageService.countFeatured(products));
        model.addAttribute("lowStockCount", productCatalogPageService.countLowStock(products));
        model.addAttribute("renderedAt", productCatalogPageService.renderedAt());
        return VIEW_NAME;
    }
}
```

</details>

### 4.2 `ProductCatalogPageService.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task11_view_template_data_output/service/ProductCatalogPageService.java`
- 역할: 제품 목록, 카테고리 옵션, 화면 요약 수치 생성을 담당하는 서비스
- 상세 설명:
- 핵심 공개 메서드: `findProducts(String categoryCode)`, `categoryOptions()`, `resolveCategory(String categoryCode)`, `countFeatured(List<ProductShowcaseItem>)`, `countLowStock(List<ProductShowcaseItem>)`, `renderedAt()`
- 트랜잭션은 사용하지 않으며, 저장소 계층 없이 화면 실습용 데이터를 메모리에서 직접 제공합니다.
- 컨트롤러가 분류 해석과 목록 조회를 서비스에 위임하므로, 컨트롤러는 화면 조립에만 집중합니다.

<details>
<summary><code>ProductCatalogPageService.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.service;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.domain.ProductShowcaseItem;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ProductCatalogPageService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<ProductShowcaseItem> products = List.of(
            new ProductShowcaseItem(
                    1L,
                    "스프링 MVC 핸드북",
                    "mvc",
                    "Spring MVC",
                    28000,
                    12,
                    true,
                    "DispatcherServlet, 요청 매핑, 검증 흐름을 한 권에 정리한 입문용 자료입니다."
            ),
            new ProductShowcaseItem(
                    2L,
                    "Thymeleaf 실전 가이드",
                    "mvc",
                    "Spring MVC",
                    32000,
                    4,
                    false,
                    "th:text, th:each, 조건식으로 서버 렌더링 페이지를 구성하는 방법을 다룹니다."
            ),
            new ProductShowcaseItem(
                    3L,
                    "클라우드 배포 체크리스트",
                    "cloud",
                    "Cloud",
                    18000,
                    9,
                    true,
                    "배포 전 점검 항목과 운영 환경에서 꼭 확인할 설정을 빠르게 훑어볼 수 있습니다."
            ),
            new ProductShowcaseItem(
                    4L,
                    "API Gateway 패턴 노트",
                    "cloud",
                    "Cloud",
                    24000,
                    3,
                    false,
                    "마이크로서비스 진입점 설계와 공통 정책 적용 포인트를 예제로 설명합니다."
            ),
            new ProductShowcaseItem(
                    5L,
                    "JPA 빠른 시작",
                    "data",
                    "Data",
                    26000,
                    2,
                    false,
                    "엔티티 매핑, 영속성 컨텍스트, 트랜잭션 기초를 실습 중심으로 정리합니다."
            )
    );

    public List<ProductShowcaseItem> findProducts(String categoryCode) {
        if ("all".equals(categoryCode)) {
            return products;
        }

        return products.stream()
                .filter(product -> product.getCategoryCode().equals(categoryCode))
                .toList();
    }

    public Map<String, String> categoryOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("all", "전체");
        options.put("mvc", "Spring MVC");
        options.put("cloud", "Cloud");
        options.put("data", "Data");
        return options;
    }

    public String resolveCategory(String categoryCode) {
        return categoryOptions().containsKey(categoryCode) ? categoryCode : "all";
    }

    public long countFeatured(List<ProductShowcaseItem> filteredProducts) {
        return filteredProducts.stream()
                .filter(ProductShowcaseItem::isFeatured)
                .count();
    }

    public long countLowStock(List<ProductShowcaseItem> filteredProducts) {
        return filteredProducts.stream()
                .filter(ProductShowcaseItem::isLowStock)
                .count();
    }

    public String renderedAt() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }
}
```

</details>

### 4.3 `ProductShowcaseItem.java`

- 파일 경로: `src/main/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task11_view_template_data_output/domain/ProductShowcaseItem.java`
- 역할: 템플릿에서 출력할 제품 한 건을 표현하는 도메인 객체
- 상세 설명:
- 제품 식별자, 이름, 분류, 가격, 재고, 추천 여부, 설명을 보관하는 읽기 전용 객체입니다.
- `isLowStock()`, `getFormattedPrice()`, `getStockStatusText()` 같은 계산 메서드를 제공해 템플릿이 복잡한 로직을 직접 갖지 않도록 했습니다.
- 템플릿에서는 이 객체 리스트를 반복하면서 각 카드의 제목, 설명, 재고 상태를 출력합니다.

<details>
<summary><code>ProductShowcaseItem.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.domain;

public class ProductShowcaseItem {

    private final Long id;
    private final String name;
    private final String categoryCode;
    private final String categoryLabel;
    private final int price;
    private final int stockQuantity;
    private final boolean featured;
    private final String summary;

    public ProductShowcaseItem(
            Long id,
            String name,
            String categoryCode,
            String categoryLabel,
            int price,
            int stockQuantity,
            boolean featured,
            String summary
    ) {
        this.id = id;
        this.name = name;
        this.categoryCode = categoryCode;
        this.categoryLabel = categoryLabel;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.featured = featured;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public int getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public boolean isFeatured() {
        return featured;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isLowStock() {
        return stockQuantity <= 5;
    }

    public String getFormattedPrice() {
        return String.format("%,d원", price);
    }

    public String getStockStatusText() {
        return isLowStock() ? "재고 확인 필요" : "즉시 구매 가능";
    }
}
```

</details>

### 4.4 `product-catalog.html`

- 파일 경로: `src/main/resources/templates/mission04/task11/product-catalog.html`
- 역할: 제품 카드 목록을 동적으로 출력하는 Thymeleaf 템플릿
- 상세 설명:
- `th:text`로 제목, 요약 수치, 제품명, 가격, 재고 상태를 출력합니다.
- `th:each="product : ${products}"`로 제품 카드 목록을 반복 렌더링하고, `th:if`로 추천 배지와 빈 목록 화면을 분기합니다.
- `th:classappend`를 사용해 재고 상태에 따라 배지 색상을 다르게 적용했습니다.

<details>
<summary><code>product-catalog.html</code> 전체 코드</summary>

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${pageTitle}">뷰 템플릿으로 출력하는 제품 목록</title>
    <style>
        :root {
            --ink: #1f2937;
            --muted: #6b7280;
            --line: #d7dee7;
            --surface: rgba(255, 255, 255, 0.88);
            --surface-strong: #ffffff;
            --accent: #0f766e;
            --accent-soft: #d9f3ef;
            --warn: #b45309;
            --warn-soft: #fff1d6;
            --bg: linear-gradient(135deg, #eef6ff 0%, #f8f3ea 52%, #f4fbf9 100%);
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: "Pretendard", "Apple SD Gothic Neo", "Noto Sans KR", sans-serif;
            color: var(--ink);
            background: var(--bg);
        }

        .page {
            max-width: 1120px;
            margin: 0 auto;
            padding: 48px 20px 80px;
        }

        .hero {
            padding: 32px;
            border: 1px solid rgba(255, 255, 255, 0.75);
            border-radius: 28px;
            background: linear-gradient(145deg, rgba(255, 255, 255, 0.95), rgba(247, 251, 255, 0.82));
            box-shadow: 0 18px 45px rgba(31, 41, 55, 0.08);
        }

        .eyebrow {
            display: inline-flex;
            padding: 8px 14px;
            border-radius: 999px;
            background: #eff6ff;
            color: #1d4ed8;
            font-size: 13px;
            font-weight: 700;
            letter-spacing: 0.04em;
            text-transform: uppercase;
        }

        h1, h2, p {
            margin: 0;
        }

        h1 {
            margin-top: 18px;
            font-size: clamp(2rem, 4vw, 3.6rem);
            line-height: 1.08;
        }

        .hero p {
            margin-top: 16px;
            max-width: 700px;
            color: var(--muted);
            font-size: 1rem;
            line-height: 1.8;
        }

        .filter-list {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 24px;
        }

        .filter-link {
            display: inline-flex;
            align-items: center;
            padding: 10px 16px;
            border-radius: 999px;
            border: 1px solid var(--line);
            background: var(--surface-strong);
            color: var(--ink);
            text-decoration: none;
            font-size: 0.95rem;
            font-weight: 600;
        }

        .filter-link.active {
            border-color: transparent;
            background: var(--ink);
            color: #ffffff;
        }

        .summary-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 16px;
            margin-top: 28px;
        }

        .summary-card {
            padding: 20px;
            border: 1px solid rgba(255, 255, 255, 0.82);
            border-radius: 22px;
            background: var(--surface);
            backdrop-filter: blur(10px);
            box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
        }

        .summary-label {
            color: var(--muted);
            font-size: 0.92rem;
        }

        .summary-value {
            margin-top: 10px;
            font-size: 1.9rem;
            font-weight: 800;
        }

        .section-head {
            display: flex;
            justify-content: space-between;
            align-items: end;
            gap: 16px;
            margin-top: 36px;
        }

        .section-head p {
            color: var(--muted);
            font-size: 0.95rem;
        }

        .product-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 18px;
            margin-top: 20px;
        }

        .product-card {
            display: flex;
            flex-direction: column;
            gap: 16px;
            padding: 22px;
            border: 1px solid rgba(255, 255, 255, 0.82);
            border-radius: 24px;
            background: rgba(255, 255, 255, 0.9);
            box-shadow: 0 14px 34px rgba(15, 23, 42, 0.08);
        }

        .card-top,
        .card-bottom {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            align-items: center;
        }

        .category-pill,
        .status-pill,
        .featured-pill {
            display: inline-flex;
            align-items: center;
            padding: 6px 10px;
            border-radius: 999px;
            font-size: 0.8rem;
            font-weight: 700;
        }

        .category-pill {
            background: #eff6ff;
            color: #1d4ed8;
        }

        .status-pill.stock-ok {
            background: var(--accent-soft);
            color: var(--accent);
        }

        .status-pill.stock-low {
            background: var(--warn-soft);
            color: var(--warn);
        }

        .featured-pill {
            background: #fce7f3;
            color: #be185d;
        }

        .product-name {
            font-size: 1.25rem;
            line-height: 1.35;
        }

        .product-summary {
            color: var(--muted);
            line-height: 1.7;
            min-height: 88px;
        }

        .price {
            font-size: 1.2rem;
            font-weight: 800;
        }

        .stock-text {
            color: var(--muted);
            font-size: 0.95rem;
        }

        .empty-state {
            margin-top: 20px;
            padding: 28px;
            border: 1px dashed var(--line);
            border-radius: 24px;
            background: rgba(255, 255, 255, 0.72);
            color: var(--muted);
            text-align: center;
            line-height: 1.8;
        }

        .footer-note {
            margin-top: 18px;
            color: var(--muted);
            font-size: 0.9rem;
        }

        @media (max-width: 768px) {
            .page {
                padding-top: 28px;
            }

            .hero,
            .summary-card,
            .product-card {
                border-radius: 20px;
            }

            .section-head,
            .card-top,
            .card-bottom {
                flex-direction: column;
                align-items: start;
            }
        }
    </style>
</head>
<body>
<main class="page">
    <section class="hero">
        <span class="eyebrow">Mission 04 Task 11</span>
        <h1 th:text="${pageTitle}">뷰 템플릿으로 출력하는 제품 목록</h1>
        <p>
            컨트롤러가 `Model`에 담은 제품 목록을 Thymeleaf가 반복문으로 렌더링합니다.
            현재 선택된 분류는 <strong th:text="${selectedCategoryLabel}">전체</strong>이며,
            각 카드에는 이름, 분류, 가격, 재고 상태가 동적으로 출력됩니다.
        </p>

        <div class="filter-list">
            <a class="filter-link"
               th:classappend="${selectedCategory == 'all'} ? ' active'"
               th:href="@{/mission04/task11/products}">
                전체
            </a>
            <a class="filter-link"
               th:classappend="${selectedCategory == 'mvc'} ? ' active'"
               th:href="@{/mission04/task11/products(category='mvc')}">
                Spring MVC
            </a>
            <a class="filter-link"
               th:classappend="${selectedCategory == 'cloud'} ? ' active'"
               th:href="@{/mission04/task11/products(category='cloud')}">
                Cloud
            </a>
            <a class="filter-link"
               th:classappend="${selectedCategory == 'data'} ? ' active'"
               th:href="@{/mission04/task11/products(category='data')}">
                Data
            </a>
        </div>

        <div class="summary-grid">
            <article class="summary-card">
                <div class="summary-label">현재 화면 제품 수</div>
                <div class="summary-value" th:text="${totalCount}">5</div>
            </article>
            <article class="summary-card">
                <div class="summary-label">추천 상품 수</div>
                <div class="summary-value" th:text="${featuredCount}">2</div>
            </article>
            <article class="summary-card">
                <div class="summary-label">재고 확인 필요</div>
                <div class="summary-value" th:text="${lowStockCount}">2</div>
            </article>
            <article class="summary-card">
                <div class="summary-label">렌더링 시각</div>
                <div class="summary-value" th:text="${renderedAt}">2026-03-20 15:30:00</div>
            </article>
        </div>
    </section>

    <section>
        <div class="section-head">
            <div>
                <h2>제품 목록</h2>
                <p th:text="|선택된 분류: ${selectedCategoryLabel}|">선택된 분류: 전체</p>
            </div>
            <p>템플릿 반복문은 `th:each="product : ${products}"`로 구성했습니다.</p>
        </div>

        <div class="product-grid" th:if="${!products.isEmpty()}">
            <article class="product-card" th:each="product : ${products}">
                <div class="card-top">
                    <span class="category-pill" th:text="${product.categoryLabel}">Spring MVC</span>
                    <span class="featured-pill" th:if="${product.featured}">추천</span>
                </div>

                <div>
                    <h3 class="product-name" th:text="${product.name}">스프링 MVC 핸드북</h3>
                    <p class="product-summary" th:text="${product.summary}">
                        DispatcherServlet, 요청 매핑, 검증 흐름을 한 권에 정리한 입문용 자료입니다.
                    </p>
                </div>

                <div class="card-bottom">
                    <div>
                        <div class="price" th:text="${product.formattedPrice}">28,000원</div>
                        <div class="stock-text" th:text="|남은 수량 ${product.stockQuantity}개|">남은 수량 12개</div>
                    </div>
                    <span class="status-pill"
                          th:classappend="${product.lowStock} ? ' stock-low' : ' stock-ok'"
                          th:text="${product.stockStatusText}">
                        즉시 구매 가능
                    </span>
                </div>
            </article>
        </div>

        <div class="empty-state" th:if="${products.isEmpty()}">
            조건에 맞는 제품이 없습니다. 다른 분류를 선택해 다시 확인해 보세요.
        </div>

        <p class="footer-note">
            이 화면은 정적 HTML이 아니라, 서버에서 전달한 `products` 컬렉션 길이에 따라 카드 수가 달라집니다.
        </p>
    </section>
</main>
</body>
</html>
```

</details>

### 4.5 `ProductCatalogPageControllerTest.java`

- 파일 경로: `src/test/java/com/goorm/springmissionsplayground/mission04_spring_mvc/task11_view_template_data_output/ProductCatalogPageControllerTest.java`
- 역할: 기본 목록과 카테고리 필터 렌더링 결과를 검증하는 테스트
- 상세 설명:
- 검증 시나리오 1: 기본 요청이 전체 제품 5개를 모델에 담고 올바른 뷰를 렌더링하는지 확인합니다.
- 검증 시나리오 2: `category=cloud` 요청이 Cloud 제품 2개만 남긴 상태로 다시 렌더링되는지 확인합니다.
- 정상 흐름만 다루지만, 요청 파라미터에 따른 화면 변화가 실제 HTML에 반영되는지까지 함께 보장합니다.

<details>
<summary><code>ProductCatalogPageControllerTest.java</code> 전체 코드</summary>

```java
package com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
class ProductCatalogPageControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("기본 요청은 전체 제품 목록을 담아 Thymeleaf 페이지를 렌더링한다")
    void showAllProducts() throws Exception {
        mockMvc.perform(get("/mission04/task11/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task11/product-catalog"))
                .andExpect(model().attribute("selectedCategory", is("all")))
                .andExpect(model().attribute("selectedCategoryLabel", is("전체")))
                .andExpect(model().attribute("products", hasSize(5)))
                .andExpect(model().attribute("totalCount", is(5)))
                .andExpect(content().string(containsString("스프링 MVC 핸드북")))
                .andExpect(content().string(containsString("API Gateway 패턴 노트")))
                .andExpect(content().string(containsString("현재 화면 제품 수")));
    }

    @Test
    @DisplayName("category 파라미터를 전달하면 해당 분류 제품만 반복 출력한다")
    void showProductsByCategory() throws Exception {
        mockMvc.perform(get("/mission04/task11/products").param("category", "cloud"))
                .andExpect(status().isOk())
                .andExpect(view().name("mission04/task11/product-catalog"))
                .andExpect(model().attribute("selectedCategory", is("cloud")))
                .andExpect(model().attribute("selectedCategoryLabel", is("Cloud")))
                .andExpect(model().attribute("products", hasSize(2)))
                .andExpect(model().attribute("totalCount", is(2)))
                .andExpect(content().string(containsString("클라우드 배포 체크리스트")))
                .andExpect(content().string(containsString("API Gateway 패턴 노트")))
                .andExpect(content().string(not(containsString("JPA 빠른 시작"))));
    }
}
```

</details>

## 5. 새로 나온 개념 정리 + 참고 링크

### 5.1 Annotated Controller와 Model

- 핵심:
  - `@Controller` 메서드는 요청을 처리한 뒤 뷰 이름을 반환하고, `Model`에는 화면에 필요한 데이터를 담습니다.
  - 템플릿 엔진은 이 `Model` 값을 꺼내 HTML로 렌더링합니다.
- 왜 쓰는가:
  - 컨트롤러는 요청 해석과 화면 조립에 집중하고, 실제 HTML 출력은 템플릿에 맡길 수 있어 역할이 분리됩니다.
  - 같은 템플릿도 모델 데이터에 따라 다른 결과를 보여줄 수 있습니다.
- 참고 링크:
  - Spring Framework Reference, Annotated Controllers: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html

### 5.2 Thymeleaf `th:text`

- 핵심:
  - `th:text`는 모델 값을 안전하게 HTML 텍스트로 출력하는 대표적인 속성입니다.
  - 제목, 설명, 숫자, 시각처럼 단일 값을 템플릿에 삽입할 때 자주 사용합니다.
- 왜 쓰는가:
  - 서버에서 준비한 값을 HTML에 직접 문자열 결합하지 않고 선언적으로 연결할 수 있습니다.
  - 템플릿 안에서 어떤 값이 동적으로 바뀌는지 한눈에 보입니다.
- 참고 링크:
  - Thymeleaf Official Tutorial: https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html
  - Thymeleaf + Spring Tutorial: https://www.thymeleaf.org/doc/tutorials/2.1/thymeleafspring.html

### 5.3 Thymeleaf `th:each`

- 핵심:
  - `th:each`는 컬렉션, 배열, 맵 같은 반복 가능한 데이터를 순회하며 같은 마크업을 여러 번 생성합니다.
  - 이번 태스크에서는 `products` 목록을 카드 단위로 반복 출력했습니다.
- 왜 쓰는가:
  - 제품 수가 바뀌어도 템플릿 코드를 늘리지 않고 같은 구조의 화면을 자동으로 만들 수 있습니다.
  - 목록 화면, 테이블, 카드 그리드처럼 반복되는 UI를 서버 렌더링으로 표현할 때 가장 기본이 되는 기능입니다.
- 참고 링크:
  - Thymeleaf Official Tutorial, Iteration: https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html

## 6. 실행·검증 방법

### 6.1 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6.2 화면 접근 방법

기본 목록:

```bash
open http://localhost:8080/mission04/task11/products
```

Cloud 분류만 보기:

```bash
open "http://localhost:8080/mission04/task11/products?category=cloud"
```

터미널에서 HTML 일부 확인:

```bash
curl http://localhost:8080/mission04/task11/products
curl "http://localhost:8080/mission04/task11/products?category=cloud"
```

예상 HTML 특징:

- 기본 요청에는 `스프링 MVC 핸드북`, `Thymeleaf 실전 가이드`, `클라우드 배포 체크리스트`, `API Gateway 패턴 노트`, `JPA 빠른 시작` 카드가 모두 표시됩니다.
- `category=cloud` 요청에는 `클라우드 배포 체크리스트`, `API Gateway 패턴 노트` 카드만 남습니다.

### 6.3 테스트 실행

```bash
./gradlew test --tests "com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.ProductCatalogPageControllerTest"
```

예상 결과:

- 기본 목록 테스트가 뷰 이름, 모델 데이터, HTML 렌더링 문구를 검증합니다.
- 카테고리 필터 테스트가 Cloud 제품만 반복 출력되는지 확인합니다.

## 7. 결과 확인 방법(스크린샷 포함)

- 성공 기준:
  - `/mission04/task11/products` 접속 시 제품 카드 5개가 렌더링됩니다.
  - `/mission04/task11/products?category=cloud` 접속 시 Cloud 제품 카드 2개만 렌더링됩니다.
  - 화면 상단에 현재 화면 제품 수, 추천 상품 수, 재고 확인 필요 수, 렌더링 시각이 표시됩니다.
- 응답 결과 예시:

```text
GET /mission04/task11/products
-> HTML 응답 본문에 "스프링 MVC 핸드북", "Thymeleaf 실전 가이드", "클라우드 배포 체크리스트", "API Gateway 패턴 노트", "JPA 빠른 시작" 문자열 포함

GET /mission04/task11/products?category=cloud
-> HTML 응답 본문에 "클라우드 배포 체크리스트", "API Gateway 패턴 노트" 문자열 포함
-> HTML 응답 본문에 "JPA 빠른 시작" 문자열 미포함
```

- 스크린샷 파일명과 저장 위치:
  - 전체 목록 화면 캡처: `task11-product-catalog-all.png`, 저장 위치 `docs/mission-04-spring-mvc/task-11-view-template-data-output/task11-product-catalog-all.png`
  - Cloud 필터 화면 캡처: `task11-product-catalog-cloud.png`, 저장 위치 `docs/mission-04-spring-mvc/task-11-view-template-data-output/task11-product-catalog-cloud.png`

## 8. 학습 내용

이번 태스크에서는 컨트롤러가 HTML 문자열을 직접 만드는 것이 아니라, 화면에 필요한 데이터만 `Model`에 담아 템플릿으로 넘기면 된다는 점을 다시 확인했습니다. 컨트롤러는 어떤 제품을 보여줄지 결정하고, 템플릿은 그 데이터를 어떤 모양의 HTML로 출력할지 담당합니다. 그래서 화면 구조를 바꾸고 싶을 때는 템플릿을 수정하면 되고, 목록 필터링 규칙을 바꾸고 싶을 때는 컨트롤러나 서비스를 수정하면 됩니다.

또한 `th:each`를 사용하면 제품 수가 늘어나거나 줄어들어도 템플릿을 복붙해서 늘릴 필요가 없습니다. 서버에서 넘어온 컬렉션 크기에 맞춰 카드가 자동으로 반복 생성되기 때문입니다. 여기에 `th:if`, `th:classappend`까지 함께 사용하면 같은 목록 화면 안에서도 추천 여부, 재고 상태처럼 조건에 따른 차이를 자연스럽게 표현할 수 있습니다.
