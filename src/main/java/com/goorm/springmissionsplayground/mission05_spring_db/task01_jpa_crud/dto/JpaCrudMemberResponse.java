package com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.dto;

import com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.domain.JpaCrudMember;

public class JpaCrudMemberResponse {

    private final Long id;
    private final String name;
    private final String email;

    public JpaCrudMemberResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static JpaCrudMemberResponse from(JpaCrudMember member) {
        return new JpaCrudMemberResponse(member.getId(), member.getName(), member.getEmail());
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
