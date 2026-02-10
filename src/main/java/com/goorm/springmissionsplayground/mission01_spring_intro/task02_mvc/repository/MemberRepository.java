package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.repository;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    List<Member> findAll();
    Optional<Member> findById(Long id);
}
