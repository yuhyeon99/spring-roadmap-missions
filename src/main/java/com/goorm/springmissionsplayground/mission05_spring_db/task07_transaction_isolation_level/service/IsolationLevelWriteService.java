package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.repository.IsolationInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IsolationLevelWriteService {

    private final IsolationInventoryRepository isolationInventoryRepository;

    public IsolationLevelWriteService(IsolationInventoryRepository isolationInventoryRepository) {
        this.isolationInventoryRepository = isolationInventoryRepository;
    }

    @Transactional(
            transactionManager = "task07TransactionManager",
            propagation = Propagation.REQUIRES_NEW
    )
    public void updateQuantityInNewTransaction(Long itemId, int quantity) {
        isolationInventoryRepository.updateQuantity(itemId, quantity);
    }
}
