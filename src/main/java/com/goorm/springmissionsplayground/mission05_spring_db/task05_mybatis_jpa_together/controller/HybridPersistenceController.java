package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.controller;

import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.CategoryStockSummary;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridPersistenceSnapshotResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridStoreProductCreateRequest;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridStoreProductView;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridStoreStockUpdateRequest;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridTechnologyComparisonResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.service.HybridPersistenceService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission05/task05/products")
public class HybridPersistenceController {

    private final HybridPersistenceService hybridPersistenceService;

    public HybridPersistenceController(HybridPersistenceService hybridPersistenceService) {
        this.hybridPersistenceService = hybridPersistenceService;
    }

    @PostMapping
    public ResponseEntity<HybridPersistenceSnapshotResponse> create(
            @RequestBody @Valid HybridStoreProductCreateRequest request
    ) {
        HybridPersistenceSnapshotResponse response = hybridPersistenceService.createWithJpaAndReadWithMyBatis(
                request.getName(),
                request.getCategory(),
                request.getPrice(),
                request.getStockQuantity()
        );
        return ResponseEntity
                .created(URI.create("/mission05/task05/products/" + response.getSavedByJpa().getId()))
                .body(response);
    }

    @PatchMapping("/{id}/stock")
    public HybridPersistenceSnapshotResponse updateStock(
            @PathVariable Long id,
            @RequestBody @Valid HybridStoreStockUpdateRequest request
    ) {
        return hybridPersistenceService.updateStockWithJpaAndReadWithMyBatis(id, request.getStockQuantity());
    }

    @GetMapping("/{id}/compare")
    public HybridTechnologyComparisonResponse compare(@PathVariable Long id) {
        return hybridPersistenceService.compareJpaAndMyBatis(id);
    }

    @GetMapping("/mybatis")
    public List<HybridStoreProductView> listByMyBatis() {
        return hybridPersistenceService.readAllProductsWithMyBatis();
    }

    @GetMapping("/mybatis/category-summary")
    public List<CategoryStockSummary> categorySummaryByMyBatis() {
        return hybridPersistenceService.readCategoryStockSummariesWithMyBatis();
    }
}
