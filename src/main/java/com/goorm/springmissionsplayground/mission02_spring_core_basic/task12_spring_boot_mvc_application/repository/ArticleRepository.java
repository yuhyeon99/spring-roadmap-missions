package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.repository;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.domain.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository {

    Article save(Article article);

    Optional<Article> findById(Long id);

    List<Article> findAll();

    void deleteById(Long id);

    void clear();
}
