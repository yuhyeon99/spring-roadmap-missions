package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MemberRegistrationForm {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Size(max = 60, message = "이메일은 60자 이하여야 합니다.")
    private String email;

    @NotNull(message = "나이는 필수입니다.")
    @Min(value = 14, message = "나이는 14세 이상이어야 합니다.")
    @Max(value = 120, message = "나이는 120세 이하여야 합니다.")
    private Integer age;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "학습 트랙은 필수입니다.")
    @Size(max = 30, message = "학습 트랙 값이 너무 깁니다.")
    private String studyTrack;

    @Size(max = 120, message = "자기소개는 120자 이하여야 합니다.")
    private String introduction;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStudyTrack() {
        return studyTrack;
    }

    public void setStudyTrack(String studyTrack) {
        this.studyTrack = studyTrack;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
