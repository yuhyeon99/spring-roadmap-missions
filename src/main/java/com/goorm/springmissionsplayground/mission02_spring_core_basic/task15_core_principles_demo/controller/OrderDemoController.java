package com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.aop.ExecutionLogStore;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.domain.Order;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController("task15OrderDemoController")
@RequestMapping("/mission02/task15/orders")
public class OrderDemoController {

    private final OrderService orderService;
    private final ExecutionLogStore logStore;

    public OrderDemoController(OrderService orderService, ExecutionLogStore logStore) {
        this.orderService = orderService;
        this.logStore = logStore;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order place(@RequestBody Map<String, Object> body) {
        String item = (String) body.getOrDefault("item", "");
        int price = ((Number) body.getOrDefault("price", 0)).intValue();
        return orderService.placeOrder(item, price);
    }

    @GetMapping
    public List<Order> list() {
        return orderService.list();
    }

    @GetMapping("/logs")
    public List<String> logs() {
        return logStore.getLogs();
    }
}
