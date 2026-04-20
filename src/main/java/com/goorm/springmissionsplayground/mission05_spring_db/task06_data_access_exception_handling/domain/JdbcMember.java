package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.domain;

public class JdbcMember {

    private final Long id;
    private final String name;
    private final String email;
    private final String grade;

    public JdbcMember(Long id, String name, String email, String grade) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGrade() {
        return grade;
    }
}
