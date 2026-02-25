package com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.domain.MockMember;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.repository.MockMemberRepository;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task03_mock_object.service.MockMemberService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockMemberServiceTest {

    @Mock
    private MockMemberRepository mockMemberRepository;

    @InjectMocks
    private MockMemberService mockMemberService;

    @Test
    void createMember_usesMockRepositorySave() {
        when(mockMemberRepository.save(any(MockMember.class)))
                .thenAnswer(invocation -> {
                    MockMember member = invocation.getArgument(0);
                    member.setId(1L);
                    return member;
                });

        MockMember created = mockMemberService.createMember("Alice", "alice@example.com");

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo("Alice");
        assertThat(created.getEmail()).isEqualTo("alice@example.com");
        verify(mockMemberRepository).save(any(MockMember.class));
    }

    @Test
    void findMember_returnsMockedData() {
        when(mockMemberRepository.findById(1L))
                .thenReturn(Optional.of(new MockMember(1L, "Bob", "bob@example.com")));

        MockMember found = mockMemberService.findMember(1L);

        assertThat(found.getName()).isEqualTo("Bob");
        assertThat(found.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void listMembers_returnsMockedList() {
        when(mockMemberRepository.findAll()).thenReturn(List.of(
                new MockMember(1L, "Carol", "carol@example.com"),
                new MockMember(2L, "Dave", "dave@example.com")
        ));

        List<MockMember> members = mockMemberService.listMembers();

        assertThat(members).hasSize(2);
        assertThat(members.get(0).getName()).isEqualTo("Carol");
        assertThat(members.get(1).getName()).isEqualTo("Dave");
    }

    @Test
    void updateMember_usesMockedFindAndSave() {
        MockMember existing = new MockMember(1L, "Old Name", "old@example.com");
        when(mockMemberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(mockMemberRepository.save(any(MockMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMember updated = mockMemberService.updateMember(1L, "New Name", "new@example.com");

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        verify(mockMemberRepository).findById(1L);
        verify(mockMemberRepository).save(existing);
    }

    @Test
    void deleteMember_callsMockDeleteWhenExists() {
        when(mockMemberRepository.existsById(1L)).thenReturn(true);

        mockMemberService.deleteMember(1L);

        verify(mockMemberRepository).deleteById(1L);
    }

    @Test
    void deleteMember_throwsExceptionWhenMissing() {
        when(mockMemberRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> mockMemberService.deleteMember(9L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id=9");
    }
}
