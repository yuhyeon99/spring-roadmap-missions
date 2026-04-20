package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class JdbcMemberCreateRequest {

    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    private String name;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    private String email;

    @NotBlank(message = "등급은 비어 있을 수 없습니다.")
    private String grade;

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
