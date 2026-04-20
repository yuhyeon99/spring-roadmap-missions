package com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.controller;

import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto.JdbcMemberCreateRequest;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.dto.JdbcMemberResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task06_data_access_exception_handling.service.JdbcMemberService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission05/task06/members")
public class JdbcMemberController {

    private final JdbcMemberService jdbcMemberService;

    public JdbcMemberController(JdbcMemberService jdbcMemberService) {
        this.jdbcMemberService = jdbcMemberService;
    }

    @PostMapping
    public ResponseEntity<JdbcMemberResponse> create(@RequestBody @Valid JdbcMemberCreateRequest request) {
        JdbcMemberResponse response = jdbcMemberService.register(
                request.getName(),
                request.getEmail(),
                request.getGrade()
        );
        return ResponseEntity
                .created(URI.create("/mission05/task06/members/" + response.getId()))
                .body(response);
    }

    @GetMapping("/demo/sql-error")
    public void sqlErrorDemo() {
        jdbcMemberService.demonstrateSqlException();
    }

    @GetMapping("/{id}")
    public JdbcMemberResponse findById(@PathVariable Long id) {
        return jdbcMemberService.findById(id);
    }

    @GetMapping
    public List<JdbcMemberResponse> findAll() {
        return jdbcMemberService.findAll();
    }
}
