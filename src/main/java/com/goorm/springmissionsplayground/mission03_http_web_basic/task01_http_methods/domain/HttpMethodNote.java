package com.goorm.springmissionsplayground.mission03_http_web_basic.task01_http_methods.domain;

import java.time.LocalDateTime;

/**
 * HTTP 메서드 실습용 단순 노트 리소스.
 */
public record HttpMethodNote(
        Long id,
        String title,
        String content,
        LocalDateTime updatedAt
) {
    public HttpMethodNote withUpdatedContent(String newTitle, String newContent, LocalDateTime updatedAt) {
        return new HttpMethodNote(this.id, newTitle, newContent, updatedAt);
    }
}
