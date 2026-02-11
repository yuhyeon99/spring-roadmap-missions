package com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.service;

import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.domain.Member;
import com.goorm.springmissionsplayground.mission01_spring_intro.task04_jpa.repository.MemberJpaRepository;
import com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.exception.TxSimulationException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service("memberTxService")
@Transactional
public class MemberTxService {

    private final MemberJpaRepository memberRepository;

    public MemberTxService(MemberJpaRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member create(String name, String email) {
        Member member = new Member(name, email);
        return memberRepository.save(member);
    }

    public Member createWithFailure(String name, String email) {
        Member member = new Member(name, email);
        memberRepository.save(member);
        throw new TxSimulationException("트랜잭션 롤백 시뮬레이션");
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }
}
