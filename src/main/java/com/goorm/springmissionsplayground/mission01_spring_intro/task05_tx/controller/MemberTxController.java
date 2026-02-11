package com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.controller;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.dto.MemberRequest;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.dto.MemberResponse;
import com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.service.MemberTxService;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("memberTxController")
@RequestMapping("/mission01/task05/members")
public class MemberTxController {

    private final MemberTxService memberTxService;

    public MemberTxController(MemberTxService memberTxService) {
        this.memberTxService = memberTxService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@RequestBody MemberRequest request) {
        Member created = memberTxService.create(request.getName(), request.getEmail());
        return ResponseEntity
                .created(URI.create("/mission01/task05/members/" + created.getId()))
                .body(MemberResponse.from(created));
    }

    @PostMapping("/fail")
    public ResponseEntity<MemberResponse> createFail(@RequestBody MemberRequest request) {
        memberTxService.createWithFailure(request.getName(), request.getEmail());
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping
    public List<MemberResponse> list() {
        return memberTxService.findAll().stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MemberResponse findOne(@PathVariable Long id) {
        return MemberResponse.from(memberTxService.findById(id));
    }
}
