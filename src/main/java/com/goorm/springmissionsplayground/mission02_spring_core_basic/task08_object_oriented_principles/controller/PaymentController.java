package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.ProcessorInfoResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task08/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/processors")
    public ProcessorInfoResponse processors() {
        return paymentService.processorInfo();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse pay(@RequestBody PaymentRequest request) {
        return paymentService.pay(request);
    }
}
