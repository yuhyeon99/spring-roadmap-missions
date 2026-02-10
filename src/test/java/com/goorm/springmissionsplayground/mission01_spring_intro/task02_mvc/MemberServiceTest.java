package com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc;

import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.repository.InMemoryMemberRepository;
import com.goorm.springmissionsplayground.mission01_spring_intro.task02_mvc.service.MemberService;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MemberServiceTest {

    private final MemberService memberService = new MemberService(new InMemoryMemberRepository());

    @Test
    void createAndFindMember() {
        Member created = memberService.createMember("Alice", "alice@example.com");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Alice");

        Member found = memberService.findMember(created.getId()).orElseThrow();
        assertThat(found.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void listMembers() {
        memberService.createMember("Bob", "bob@example.com");
        memberService.createMember("Carol", "carol@example.com");

        List<Member> members = memberService.listMembers();
        assertThat(members).hasSize(2);
    }
}
