package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading;

import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingMember;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.dto.MemberTeamSummary;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository.LazyLoadingMemberRepository;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository.LazyLoadingTeamRepository;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.service.TransactionLazyLoadingStudyService;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransactionLazyLoadingStudyServiceTest {

    @Autowired
    private TransactionLazyLoadingStudyService studyService;

    @Autowired
    private LazyLoadingMemberRepository memberRepository;

    @Autowired
    private LazyLoadingTeamRepository teamRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    @DisplayName("트랜잭션이 끝난 뒤 LAZY 연관 엔터티를 접근하면 LazyInitializationException이 발생한다")
    void lazyAssociationOutsideTransactionThrowsException() {
        Long memberId = studyService.createSample("백엔드팀", "김지연");

        LazyLoadingMember member = studyService.findMember(memberId);

        assertThat(Hibernate.isInitialized(member.getTeam())).isFalse();
        assertThatThrownBy(() -> member.getTeam().getName())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    @DisplayName("트랜잭션 안에서 필요한 데이터를 DTO로 변환하면 LAZY 연관 엔터티를 안전하게 사용할 수 있다")
    void transactionalDtoMappingAvoidsLazyLoadingException() {
        Long memberId = studyService.createSample("백엔드팀", "김트랜잭션");

        MemberTeamSummary summary = studyService.readMemberTeamSummary(memberId);

        assertThat(summary.memberId()).isEqualTo(memberId);
        assertThat(summary.memberName()).isEqualTo("김트랜잭션");
        assertThat(summary.teamName()).isEqualTo("백엔드팀");
    }

    @Test
    @DisplayName("EntityGraph로 연관 엔터티를 미리 조회하면 트랜잭션 종료 후에도 필요한 값을 읽을 수 있다")
    void entityGraphPreloadsAssociationBeforeTransactionEnds() {
        Long memberId = studyService.createSample("백엔드팀", "김엔티티그래프");

        LazyLoadingMember member = studyService.findMemberWithTeam(memberId);

        assertThat(Hibernate.isInitialized(member.getTeam())).isTrue();
        assertThat(member.getTeam().getName()).isEqualTo("백엔드팀");
    }
}
