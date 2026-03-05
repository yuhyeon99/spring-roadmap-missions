package com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.repository;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    void clear();
}
