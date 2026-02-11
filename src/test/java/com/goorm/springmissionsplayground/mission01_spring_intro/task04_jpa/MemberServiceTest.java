package com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.service.MemberJpaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberJpaService memberService;

    @Test
    void create_read_update_delete() {
        Member created = memberService.create("JPA User", "jpa@example.com");

        Member found = memberService.findById(created.getId());
        assertThat(found.getName()).isEqualTo("JPA User");

        Member updated = memberService.update(created.getId(), "Updated", "updated@example.com");
        assertThat(updated.getName()).isEqualTo("Updated");

        List<Member> members = memberService.findAll();
        assertThat(members).hasSize(1);

        memberService.delete(created.getId());
        assertThat(memberService.findAll()).isEmpty();
    }
}
