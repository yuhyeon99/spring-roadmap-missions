package com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query.controller;

import com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query.dto.JdbcTemplateBookResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query.service.JdbcTemplateBookQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission05/task08/books")
public class JdbcTemplateBookQueryController {

    private final JdbcTemplateBookQueryService jdbcTemplateBookQueryService;

    public JdbcTemplateBookQueryController(JdbcTemplateBookQueryService jdbcTemplateBookQueryService) {
        this.jdbcTemplateBookQueryService = jdbcTemplateBookQueryService;
    }

    @GetMapping("/console-query")
    public List<JdbcTemplateBookResponse> queryAndPrintToConsole() {
        return jdbcTemplateBookQueryService.queryBooksAndPrintToConsole();
    }
}
