package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.domain;

public class Enrollment {

    private final String userId;
    private final Course course;
    private final int paidAmount;
    private final String transactionId;

    public Enrollment(String userId, Course course, int paidAmount, String transactionId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (course == null) {
            throw new IllegalArgumentException("과정 정보가 필요합니다.");
        }
        if (paidAmount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("거래 ID는 필수입니다.");
        }
        this.userId = userId;
        this.course = course;
        this.paidAmount = paidAmount;
        this.transactionId = transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public Course getCourse() {
        return course;
    }

    public int getPaidAmount() {
        return paidAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
