package com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.repository;

import com.goorm.springmissionsplayground.mission05_spring_db.task09_transaction_management.domain.TransferAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferAccountRepository extends JpaRepository<TransferAccount, Long> {

    Optional<TransferAccount> findByAccountNumber(String accountNumber);
}
