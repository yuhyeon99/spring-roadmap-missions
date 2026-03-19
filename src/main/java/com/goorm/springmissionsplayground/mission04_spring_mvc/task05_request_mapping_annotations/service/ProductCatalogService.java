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
