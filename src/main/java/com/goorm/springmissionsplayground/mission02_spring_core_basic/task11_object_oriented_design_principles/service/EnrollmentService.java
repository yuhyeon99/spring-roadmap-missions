package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.domain.Course;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.domain.Enrollment;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.notification.NotificationChannel;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.payment.PaymentGateway;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.payment.PaymentReceipt;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.policy.DiscountPolicy;

public class EnrollmentService {

    private final DiscountPolicy discountPolicy;
    private final PaymentGateway paymentGateway;
    private final NotificationChannel notificationChannel;

    public EnrollmentService(
        DiscountPolicy discountPolicy,
        PaymentGateway paymentGateway,
        NotificationChannel notificationChannel
    ) {
        this.discountPolicy = discountPolicy;
        this.paymentGateway = paymentGateway;
        this.notificationChannel = notificationChannel;
    }

    public Enrollment enroll(String userId, Course course) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (course == null) {
            throw new IllegalArgumentException("수강할 과정을 선택하세요.");
        }

        int payableAmount = discountPolicy.apply(course.getTuition());
        PaymentReceipt receipt = paymentGateway.pay(payableAmount);
        notificationChannel.notify(userId, buildMessage(course, receipt));

        return new Enrollment(userId, course, receipt.paidAmount(), receipt.transactionId());
    }

    private String buildMessage(Course course, PaymentReceipt receipt) {
        return "%s 과정 결제가 완료되었습니다. 금액: %d원, 거래 ID: %s"
            .formatted(course.getTitle(), receipt.paidAmount(), receipt.transactionId());
    }
}
