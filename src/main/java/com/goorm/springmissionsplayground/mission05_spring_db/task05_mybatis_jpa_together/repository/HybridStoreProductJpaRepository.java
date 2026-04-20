package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.domain.HybridStoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HybridStoreProductJpaRepository extends JpaRepository<HybridStoreProduct, Long> {
}
