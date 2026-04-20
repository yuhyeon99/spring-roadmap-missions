package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.domain.HybridStoreProduct;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.CategoryStockSummary;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridPersistenceSnapshotResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridStoreProductResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridStoreProductView;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridTechnologyComparisonResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.mapper.HybridStoreProductMapper;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.repository.HybridStoreProductJpaRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HybridPersistenceService {

    private final HybridStoreProductJpaRepository productJpaRepository;
    private final HybridStoreProductMapper productMapper;

    public HybridPersistenceService(
            HybridStoreProductJpaRepository productJpaRepository,
            HybridStoreProductMapper productMapper
    ) {
        this.productJpaRepository = productJpaRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public HybridPersistenceSnapshotResponse createWithJpaAndReadWithMyBatis(
            String name,
            String category,
            int price,
            int stockQuantity
    ) {
        HybridStoreProduct savedProduct = productJpaRepository.save(
                new HybridStoreProduct(name, category, price, stockQuantity)
        );
        productJpaRepository.flush();
        return buildSnapshot(savedProduct);
    }

    @Transactional
    public HybridPersistenceSnapshotResponse updateStockWithJpaAndReadWithMyBatis(Long id, int stockQuantity) {
        HybridStoreProduct product = productJpaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.changeStockQuantity(stockQuantity);
        productJpaRepository.flush();
        return buildSnapshot(product);
    }

    @Transactional(readOnly = true)
    public List<HybridStoreProductView> readAllProductsWithMyBatis() {
        return productMapper.findAll();
    }

    @Transactional(readOnly = true)
    public List<CategoryStockSummary> readCategoryStockSummariesWithMyBatis() {
        return productMapper.findCategoryStockSummaries();
    }

    @Transactional(readOnly = true)
    public HybridTechnologyComparisonResponse compareJpaAndMyBatis(Long id) {
        HybridStoreProduct jpaProduct = productJpaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        HybridStoreProductView myBatisProduct = productMapper.findById(id);
        return new HybridTechnologyComparisonResponse(
                "JPA",
                "MyBatis",
                HybridStoreProductResponse.from(jpaProduct),
                myBatisProduct,
                productMapper.findCategoryStockSummaries()
        );
    }

    private HybridPersistenceSnapshotResponse buildSnapshot(HybridStoreProduct savedProduct) {
        HybridStoreProductView myBatisView = productMapper.findById(savedProduct.getId());
        return new HybridPersistenceSnapshotResponse(
                "JPA",
                "MyBatis",
                HybridStoreProductResponse.from(savedProduct),
                myBatisView,
                productMapper.findCategoryStockSummaries()
        );
    }
}
