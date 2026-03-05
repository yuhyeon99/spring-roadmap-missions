package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(Long id) {
        super("게시글을 찾을 수 없습니다. id=" + id);
    }
}
