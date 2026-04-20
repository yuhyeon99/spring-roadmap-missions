package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.domain.JdbcMember;

public class JdbcMemberResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String grade;

    public JdbcMemberResponse(Long id, String name, String email, String grade) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.grade = grade;
    }

    public static JdbcMemberResponse from(JdbcMember member) {
        return new JdbcMemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getGrade()
        );
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
