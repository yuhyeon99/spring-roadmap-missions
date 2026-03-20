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
