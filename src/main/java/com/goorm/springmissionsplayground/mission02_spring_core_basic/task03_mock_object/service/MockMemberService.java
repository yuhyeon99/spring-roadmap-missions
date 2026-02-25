package com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.domain.MockMember;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.repository.MockMemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MockMemberService {

    private final MockMemberRepository mockMemberRepository;

    public MockMemberService(MockMemberRepository mockMemberRepository) {
        this.mockMemberRepository = mockMemberRepository;
    }

    public MockMember createMember(String name, String email) {
        MockMember newMember = new MockMember(null, name, email);
        return mockMemberRepository.save(newMember);
    }

    public MockMember findMember(Long id) {
        return mockMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + id));
    }

    public List<MockMember> listMembers() {
        return mockMemberRepository.findAll();
    }

    public MockMember updateMember(Long id, String name, String email) {
        MockMember member = findMember(id);
        member.update(name, email);
        return mockMemberRepository.save(member);
    }

    public void deleteMember(Long id) {
        if (!mockMemberRepository.existsById(id)) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. id=" + id);
        }
        mockMemberRepository.deleteById(id);
    }
}
