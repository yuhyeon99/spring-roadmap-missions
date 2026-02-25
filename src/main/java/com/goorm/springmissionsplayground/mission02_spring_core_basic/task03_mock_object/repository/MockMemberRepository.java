package com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.repository;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.domain.MockMember;
import java.util.List;
import java.util.Optional;

public interface MockMemberRepository {
    MockMember save(MockMember member);

    Optional<MockMember> findById(Long id);

    List<MockMember> findAll();

    boolean existsById(Long id);

    void deleteById(Long id);
}
