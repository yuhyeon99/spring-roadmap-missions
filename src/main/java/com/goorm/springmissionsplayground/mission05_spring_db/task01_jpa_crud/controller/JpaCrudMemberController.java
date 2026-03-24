package com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.controller;

import com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.domain.JpaCrudMember;
import com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.dto.JpaCrudMemberRequest;
import com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.dto.JpaCrudMemberResponse;
import com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.service.JpaCrudMemberService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission05/task01/members")
public class JpaCrudMemberController {

    private final JpaCrudMemberService memberService;

    public JpaCrudMemberController(JpaCrudMemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<JpaCrudMemberResponse> create(@RequestBody @Valid JpaCrudMemberRequest request) {
        JpaCrudMember created = memberService.create(request.getName(), request.getEmail());
        return ResponseEntity
                .created(URI.create("/mission05/task01/members/" + created.getId()))
                .body(JpaCrudMemberResponse.from(created));
    }

    @GetMapping
    public List<JpaCrudMemberResponse> list() {
        return memberService.findAll().stream()
                .map(JpaCrudMemberResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public JpaCrudMemberResponse get(@PathVariable Long id) {
        return JpaCrudMemberResponse.from(memberService.findById(id));
    }

    @PutMapping("/{id}")
    public JpaCrudMemberResponse update(@PathVariable Long id, @RequestBody @Valid JpaCrudMemberRequest request) {
        JpaCrudMember updated = memberService.update(id, request.getName(), request.getEmail());
        return JpaCrudMemberResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }
}
