package com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mission05_task09_transfer_accounts")
public class TransferAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String accountNumber;

    @Column(nullable = false, length = 30)
    private String ownerName;

    @Column(nullable = false)
    private int balance;

    protected TransferAccount() {
        // JPA 기본 생성자
    }

    public TransferAccount(String accountNumber, String ownerName, int balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public void withdraw(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("출금 금액은 0보다 커야 합니다.");
        }
        if (balance < amount) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.balance -= amount;
    }

    public void deposit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다.");
        }
        this.balance += amount;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getBalance() {
        return balance;
    }
}
