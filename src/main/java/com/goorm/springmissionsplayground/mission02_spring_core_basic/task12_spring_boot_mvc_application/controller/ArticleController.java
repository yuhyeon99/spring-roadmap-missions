package com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleListResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.dto.ArticleResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task12_spring_boot_mvc_application.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task12/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse create(@RequestBody @Valid ArticleCreateRequest request) {
        return articleService.create(request);
    }

    @GetMapping("/{id}")
    public ArticleResponse get(@PathVariable Long id) {
        return articleService.get(id);
    }

    @GetMapping
    public ArticleListResponse list() {
        return articleService.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        articleService.delete(id);
    }
}
