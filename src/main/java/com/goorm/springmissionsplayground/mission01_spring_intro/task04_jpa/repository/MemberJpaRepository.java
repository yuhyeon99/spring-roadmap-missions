package com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.repository;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberJpaRepository extends JpaRepository<Member, Long> {
}
