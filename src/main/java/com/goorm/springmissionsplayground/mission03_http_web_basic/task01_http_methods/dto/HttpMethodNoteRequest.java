package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.dto;

import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public record HttpMethodNoteRequest(String title, String content) {

    public void validate() {
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title과 content는 필수입니다.");
        }
    }
}
