package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.domain;

public class Course {

    private final String id;
    private final String title;
    private final int tuition;

    public Course(String id, String title, int tuition) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("과정 ID는 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("과정명은 필수입니다.");
        }
        if (tuition <= 0) {
            throw new IllegalArgumentException("수강료는 0보다 커야 합니다.");
        }
        this.id = id;
        this.title = title;
        this.tuition = tuition;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTuition() {
        return tuition;
    }
}
