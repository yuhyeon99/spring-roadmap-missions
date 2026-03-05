package com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.aop.LogExecution;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.domain.Order;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("task15OrderService")
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @LogExecution
    @Transactional
    public Order placeOrder(String item, int price) {
        Order order = new Order(null, item, price);
        return orderRepository.save(order);
    }

    public List<Order> list() {
        return orderRepository.findAll();
    }
}
