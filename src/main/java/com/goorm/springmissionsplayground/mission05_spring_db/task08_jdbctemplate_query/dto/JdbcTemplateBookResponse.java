package com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query.dto;

import com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query.domain.JdbcTemplateBook;

public record JdbcTemplateBookResponse(Long id, String title, String author, String level) {

    public static JdbcTemplateBookResponse from(JdbcTemplateBook book) {
        return new JdbcTemplateBookResponse(
                book.id(),
                book.title(),
                book.author(),
                book.level()
        );
    }
}
