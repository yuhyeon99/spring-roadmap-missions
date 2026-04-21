package com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query.dto.JdbcTemplateBookResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task08_jdbctemplate_query.repository.JdbcTemplateBookRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JdbcTemplateBookQueryService {

    private final JdbcTemplateBookRepository jdbcTemplateBookRepository;

    public JdbcTemplateBookQueryService(JdbcTemplateBookRepository jdbcTemplateBookRepository) {
        this.jdbcTemplateBookRepository = jdbcTemplateBookRepository;
    }

    public List<JdbcTemplateBookResponse> queryBooksAndPrintToConsole() {
        List<JdbcTemplateBookResponse> responses = jdbcTemplateBookRepository.findAll()
                .stream()
                .map(JdbcTemplateBookResponse::from)
                .toList();

        System.out.println("=== mission05 task08 JdbcTemplate 조회 결과 시작 ===");
        for (JdbcTemplateBookResponse response : responses) {
            System.out.println("id=%d, title=%s, author=%s, level=%s".formatted(
                    response.id(),
                    response.title(),
                    response.author(),
                    response.level()
            ));
        }
        System.out.println("=== mission05 task08 JdbcTemplate 조회 결과 끝 ===");

        return responses;
    }

    public void resetSampleData() {
        jdbcTemplateBookRepository.resetSampleData();
    }
}
