package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.domain.Course;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.domain.Enrollment;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.notification.EmailNotificationChannel;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.notification.NotificationChannel;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.notification.SmsNotificationChannel;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.payment.CardPaymentGateway;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.payment.VirtualAccountPaymentGateway;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.policy.NewStudentDiscountPolicy;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.policy.NoDiscountPolicy;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.service.EnrollmentService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentServiceTest {

    @Test
    void enroll_appliesDiscount_thenPaysAndNotifies_viaCard() {
        Course course = new Course("C-101", "스프링 입문", 10000);
        NewStudentDiscountPolicy discountPolicy = new NewStudentDiscountPolicy(20);
        EmailNotificationChannel emailChannel = new EmailNotificationChannel();
        EnrollmentService enrollmentService = new EnrollmentService(
            discountPolicy,
            new CardPaymentGateway(),
            emailChannel
        );

        Enrollment enrollment = enrollmentService.enroll("user-1", course);

        assertThat(enrollment.getPaidAmount()).isEqualTo(8000);
        assertThat(enrollment.getTransactionId()).startsWith("CARD-");
        assertThat(emailChannel.deliveredMessages())
            .singleElement()
            .satisfies(msg -> assertThat(msg)
                .contains("스프링 입문")
                .contains("8000"));
    }

    @Test
    void enroll_switchPaymentImplementation_withoutChangingService() {
        Course course = new Course("C-102", "객체 지향 설계", 12000);
        EnrollmentService enrollmentService = new EnrollmentService(
            new NoDiscountPolicy(),
            new VirtualAccountPaymentGateway(),
            new SmsNotificationChannel()
        );

        Enrollment enrollment = enrollmentService.enroll("user-2", course);

        assertThat(enrollment.getPaidAmount()).isEqualTo(12000);
        assertThat(enrollment.getTransactionId()).startsWith("VA-");
    }

    @Test
    void enroll_allowsNotificationChannelReplacement_smallInterface() {
        Course course = new Course("C-103", "테스트 주도 개발", 9000);
        RecordingNotificationChannel recordingChannel = new RecordingNotificationChannel();
        EnrollmentService enrollmentService = new EnrollmentService(
            new NoDiscountPolicy(),
            new CardPaymentGateway(),
            recordingChannel
        );

        enrollmentService.enroll("user-3", course);

        assertThat(recordingChannel.lastUserId).isEqualTo("user-3");
        assertThat(recordingChannel.lastMessage).contains("테스트 주도 개발");
    }

    private static class RecordingNotificationChannel implements NotificationChannel {
        private String lastUserId;
        private String lastMessage;

        @Override
        public void notify(String userId, String message) {
            this.lastUserId = userId;
            this.lastMessage = message;
        }
    }
}
