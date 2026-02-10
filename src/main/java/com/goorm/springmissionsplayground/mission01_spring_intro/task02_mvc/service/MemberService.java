package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.service;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member createMember(String name, String email) {
        Member member = new Member(name, email);
        return memberRepository.save(member);
    }

    public List<Member> listMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> findMember(Long id) {
        return memberRepository.findById(id);
    }
}
