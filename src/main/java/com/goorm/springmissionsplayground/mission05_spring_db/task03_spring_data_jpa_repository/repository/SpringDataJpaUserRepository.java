package com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task03_spring_data_jpa_repository.domain.SpringDataJpaUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaUserRepository extends JpaRepository<SpringDataJpaUser, Long> {

    Optional<SpringDataJpaUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
