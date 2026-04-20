package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.dto;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.domain.SpringDataJpaUser;

public class SpringDataJpaUserResponse {

    private final Long id;
    private final String name;
    private final String email;

    public SpringDataJpaUserResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static SpringDataJpaUserResponse from(SpringDataJpaUser user) {
        return new SpringDataJpaUserResponse(user.getId(), user.getName(), user.getEmail());
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
}
