package com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.domain.JpaCrudMember;
import com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.repository.JpaCrudMemberRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class JpaCrudMemberService {

    private final JpaCrudMemberRepository memberRepository;

    public JpaCrudMemberService(JpaCrudMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public JpaCrudMember create(String name, String email) {
        JpaCrudMember member = new JpaCrudMember(name, email);
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<JpaCrudMember> findAll() {
        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional(readOnly = true)
    public JpaCrudMember findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    public JpaCrudMember update(Long id, String name, String email) {
        JpaCrudMember member = findById(id);
        member.updateProfile(name, email);
        return member;
    }

    public void delete(Long id) {
        JpaCrudMember member = findById(id);
        memberRepository.delete(member);
    }
}
