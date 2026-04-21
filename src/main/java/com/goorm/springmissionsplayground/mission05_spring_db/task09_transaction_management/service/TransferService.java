package com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.domain.TransferAccount;
import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.exception.TransferSimulationException;
import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.repository.TransferAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final TransferAccountRepository transferAccountRepository;

    public TransferService(TransferAccountRepository transferAccountRepository) {
        this.transferAccountRepository = transferAccountRepository;
    }

    public Long createAccount(String accountNumber, String ownerName, int balance) {
        TransferAccount account = transferAccountRepository.save(
                new TransferAccount(accountNumber, ownerName, balance)
        );
        return account.getId();
    }

    @Transactional(transactionManager = "transactionManager")
    public void transfer(String fromAccountNumber, String toAccountNumber, int amount, boolean failAfterWithdraw) {
        TransferAccount fromAccount = getAccount(fromAccountNumber);
        TransferAccount toAccount = getAccount(toAccountNumber);

        fromAccount.withdraw(amount);

        if (failAfterWithdraw) {
            throw new TransferSimulationException("출금 후 입금 전에 오류가 발생해 전체 송금을 롤백합니다.");
        }

        toAccount.deposit(amount);
    }

    @Transactional(transactionManager = "transactionManager", readOnly = true)
    public int getBalance(String accountNumber) {
        return getAccount(accountNumber).getBalance();
    }

    private TransferAccount getAccount(String accountNumber) {
        return transferAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다. accountNumber=" + accountNumber));
    }
}
