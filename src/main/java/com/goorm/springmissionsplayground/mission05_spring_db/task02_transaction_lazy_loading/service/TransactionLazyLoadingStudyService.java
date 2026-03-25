package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingMember;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain.LazyLoadingTeam;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.dto.MemberTeamSummary;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository.LazyLoadingMemberRepository;
import com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.repository.LazyLoadingTeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionLazyLoadingStudyService {

    private final LazyLoadingMemberRepository memberRepository;
    private final LazyLoadingTeamRepository teamRepository;

    public TransactionLazyLoadingStudyService(
            LazyLoadingMemberRepository memberRepository,
            LazyLoadingTeamRepository teamRepository
    ) {
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public Long createSample(String teamName, String memberName) {
        LazyLoadingTeam team = teamRepository.save(new LazyLoadingTeam(teamName));
        LazyLoadingMember member = memberRepository.save(new LazyLoadingMember(memberName, team));
        return member.getId();
    }

    @Transactional(readOnly = true)
    public LazyLoadingMember findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public MemberTeamSummary readMemberTeamSummary(Long memberId) {
        LazyLoadingMember member = findMember(memberId);
        return new MemberTeamSummary(member.getId(), member.getName(), member.getTeam().getName());
    }

    @Transactional(readOnly = true)
    public LazyLoadingMember findMemberWithTeam(Long memberId) {
        return memberRepository.findByIdWithTeam(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
