package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.domain;

import java.time.LocalDateTime;

public class Article {

    private final Long id;
    private final String title;
    private final String content;
    private final String author;
    private final LocalDateTime createdAt;

    public Article(Long id, String title, String content, String author, LocalDateTime createdAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 비어 있을 수 없습니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("본문은 비어 있을 수 없습니다.");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("작성자는 비어 있을 수 없습니다.");
        }
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
