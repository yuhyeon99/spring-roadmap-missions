package com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task01_jpa_crud.domain.JpaCrudMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCrudMemberRepository extends JpaRepository<JpaCrudMember, Long> {
}
