package com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.domain;

public class MockMember {

    private Long id;
    private String name;
    private String email;

    public MockMember(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void update(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
