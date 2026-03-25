package com.goorm.springmissionsplayground.mission05_spring_db.task02_transaction_lazy_loading.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "mission05_task02_members")
public class LazyLoadingMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private LazyLoadingTeam team;

    protected LazyLoadingMember() {
        // JPA 기본 생성자
    }

    public LazyLoadingMember(String name, LazyLoadingTeam team) {
        this.name = name;
        this.team = team;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LazyLoadingTeam getTeam() {
        return team;
    }
}
