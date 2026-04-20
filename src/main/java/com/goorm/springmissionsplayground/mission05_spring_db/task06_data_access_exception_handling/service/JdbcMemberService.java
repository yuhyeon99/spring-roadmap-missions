package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.domain.JdbcMember;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto.JdbcMemberResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.exception.JdbcMemberNotFoundException;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.repository.JdbcMemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JdbcMemberService {

    private final JdbcMemberRepository jdbcMemberRepository;

    public JdbcMemberService(JdbcMemberRepository jdbcMemberRepository) {
        this.jdbcMemberRepository = jdbcMemberRepository;
    }

    public JdbcMemberResponse register(String name, String email, String grade) {
        JdbcMember member = jdbcMemberRepository.save(name, email, grade);
        return JdbcMemberResponse.from(member);
    }

    public JdbcMemberResponse findById(Long id) {
        return jdbcMemberRepository.findById(id)
                .map(JdbcMemberResponse::from)
                .orElseThrow(() -> new JdbcMemberNotFoundException(id));
    }

    public List<JdbcMemberResponse> findAll() {
        return jdbcMemberRepository.findAll()
                .stream()
                .map(JdbcMemberResponse::from)
                .toList();
    }

    public void demonstrateSqlException() {
        jdbcMemberRepository.executeBrokenSelect();
    }
}
