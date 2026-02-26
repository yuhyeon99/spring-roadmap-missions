package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.ProcessorInfoResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void processorInfo_containsAllPaymentImplementations() {
        ProcessorInfoResponse info = paymentService.processorInfo();

        assertThat(info.getMethodToBean()).hasSize(3);
        assertThat(info.getMethodToBean())
                .containsEntry("card", "cardPaymentProcessor")
                .containsEntry("kakaopay", "kakaoPayPaymentProcessor")
                .containsEntry("bank", "bankTransferPaymentProcessor");
    }

    @Test
    void pay_selectsImplementationByMethod_usingPolymorphism() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("order-8001");
        request.setAmount(10000);
        request.setMethod("kakaopay");

        PaymentResponse response = paymentService.pay(request);

        assertThat(response.getProcessorBean()).isEqualTo("kakaoPayPaymentProcessor");
        assertThat(response.getFee()).isEqualTo(200);
        assertThat(response.getApprovedAmount()).isEqualTo(9800);
        assertThat(response.getMessage()).contains("2%");
    }

    @Test
    void pay_throwsWhenMethodIsUnsupported() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("order-8002");
        request.setAmount(10000);
        request.setMethod("crypto");

        assertThatThrownBy(() -> paymentService.pay(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 결제 방식");
    }
}
