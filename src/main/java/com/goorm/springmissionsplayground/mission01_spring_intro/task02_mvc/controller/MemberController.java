package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.controller;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.dto.MemberRequest;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.dto.MemberResponse;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.repository.InMemoryMemberRepository;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.service.MemberService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/mission01/task02/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createMember(@RequestBody MemberRequest request) {
        Member created = memberService.createMember(request.getName(), request.getEmail());
        return MemberResponse.from(created);
    }

    @GetMapping
    public List<MemberResponse> listMembers() {
        return memberService.listMembers().stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MemberResponse findMember(@PathVariable Long id) {
        Member member = memberService.findMember(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        return MemberResponse.from(member);
    }
}
