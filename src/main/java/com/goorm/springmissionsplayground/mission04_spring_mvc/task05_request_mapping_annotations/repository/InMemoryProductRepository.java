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
