package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.ProcessorInfoResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment.PaymentProcessor;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PaymentService {

    private final Map<String, PaymentProcessor> processorByMethod = new LinkedHashMap<>();
    private final Map<String, String> beanNameByMethod = new LinkedHashMap<>();

    public PaymentService(Map<String, PaymentProcessor> paymentProcessorBeans) {
        paymentProcessorBeans.forEach((beanName, processor) -> {
            String method = processor.methodKey();
            processorByMethod.put(method, processor);
            beanNameByMethod.put(method, beanName);
        });
    }

    public PaymentResponse pay(PaymentRequest request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("amount는 0보다 커야 합니다.");
        }

        String method = normalizeMethod(request.getMethod());
        PaymentProcessor paymentProcessor = processorByMethod.get(method);
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("지원하지 않는 결제 방식입니다. method=" + method);
        }

        PaymentExecution execution = paymentProcessor.pay(request.getAmount());
        return new PaymentResponse(
                request.getOrderId(),
                method,
                beanNameByMethod.get(method),
                request.getAmount(),
                execution.getFee(),
                execution.getApprovedAmount(),
                execution.getDetail()
        );
    }

    public ProcessorInfoResponse processorInfo() {
        return new ProcessorInfoResponse(
                new LinkedHashMap<>(beanNameByMethod),
                "스프링 컨테이너가 PaymentProcessor 구현체들을 모두 빈으로 등록하고, 요청 결제 타입에 맞는 구현체를 런타임에 선택합니다."
        );
    }

    private String normalizeMethod(String method) {
        if (!StringUtils.hasText(method)) {
            throw new IllegalArgumentException("method는 필수입니다.");
        }
        return method.trim().toLowerCase(Locale.ROOT);
    }
}
