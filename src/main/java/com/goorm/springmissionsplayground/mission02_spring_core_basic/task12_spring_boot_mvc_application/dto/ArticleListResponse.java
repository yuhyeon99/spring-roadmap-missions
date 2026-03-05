package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto;

import java.util.List;

public class ArticleListResponse {

    private final List<ArticleResponse> articles;

    public ArticleListResponse(List<ArticleResponse> articles) {
        this.articles = articles;
    }

    public List<ArticleResponse> getArticles() {
        return articles;
    }
}
