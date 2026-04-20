package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.mapper;

import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.CategoryStockSummary;
import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto.HybridStoreProductView;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HybridStoreProductMapper {

    @Select("""
            SELECT id,
                   name,
                   category,
                   price,
                   stock_quantity AS stockQuantity
            FROM mission05_task05_products
            WHERE id = #{id}
            """)
    HybridStoreProductView findById(@Param("id") Long id);

    @Select("""
            SELECT id,
                   name,
                   category,
                   price,
                   stock_quantity AS stockQuantity
            FROM mission05_task05_products
            ORDER BY id ASC
            """)
    List<HybridStoreProductView> findAll();

    @Select("""
            SELECT category,
                   COUNT(*) AS productCount,
                   COALESCE(SUM(stock_quantity), 0) AS totalStockQuantity
            FROM mission05_task05_products
            GROUP BY category
            ORDER BY category ASC
            """)
    List<CategoryStockSummary> findCategoryStockSummaries();
}
