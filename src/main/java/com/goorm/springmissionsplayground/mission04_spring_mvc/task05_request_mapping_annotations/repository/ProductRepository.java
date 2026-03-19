package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.repository;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.domain.Product;
import java.util.List;

public interface ProductRepository {

    List<Product> findAll();

    Product save(Product product);
}
