package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception;

public class JdbcMemberNotFoundException extends RuntimeException {

    public JdbcMemberNotFoundException(Long id) {
        super("ID가 " + id + "인 회원을 찾을 수 없습니다.");
    }
}
