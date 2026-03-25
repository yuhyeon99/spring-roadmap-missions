package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LazyLoadingTeamRepository extends JpaRepository<LazyLoadingTeam, Long> {
}
