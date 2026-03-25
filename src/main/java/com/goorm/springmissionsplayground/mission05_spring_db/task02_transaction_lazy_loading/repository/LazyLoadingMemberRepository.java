package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LazyLoadingMemberRepository extends JpaRepository<LazyLoadingMember, Long> {

    @EntityGraph(attributePaths = "team")
    @Query("select member from LazyLoadingMember member where member.id = :id")
    Optional<LazyLoadingMember> findByIdWithTeam(Long id);
}
