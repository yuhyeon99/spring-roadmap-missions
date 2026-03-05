package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.domain.Article;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleListResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.exception.ArticleNotFoundException;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article saved = articleRepository.save(new Article(
            null,
            request.getTitle(),
            request.getContent(),
            request.getAuthor(),
            LocalDateTime.now()
        ));
        return toResponse(saved);
    }

    public ArticleResponse get(Long id) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ArticleNotFoundException(id));
        return toResponse(article);
    }

    public ArticleListResponse list() {
        List<ArticleResponse> responses = articleRepository.findAll().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .map(this::toResponse)
            .collect(Collectors.toList());
        return new ArticleListResponse(responses);
    }

    @Transactional
    public void delete(Long id) {
        articleRepository.findById(id).orElseThrow(() -> new ArticleNotFoundException(id));
        articleRepository.deleteById(id);
    }

    private ArticleResponse toResponse(Article article) {
        return new ArticleResponse(
            article.getId(),
            article.getTitle(),
            article.getContent(),
            article.getAuthor(),
            article.getCreatedAt()
        );
    }
}
